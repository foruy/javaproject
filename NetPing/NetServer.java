import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

class NetTable implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ip;
    private boolean valid;

    public NetTable(String ip, boolean valid) {
        this.ip = ip;
        this.valid = valid;
    }

    public String getIp() {
        return ip;
    }

    public boolean isValid() {
        return valid;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String toString() {
        return ip;
    }
}


public class NetServer {
    private static int id = -1;
    private static final int MAXNUM = 3;
    private static final int PORT = 1234;
    private static Map<Integer,NetTable[]> netable = new HashMap<Integer,NetTable[]>();
    private DatagramSocket dgs = null;

    public long inet_aton(String strIP) throws Exception {
        byte[] bAddr = InetAddress.getByName(strIP).getAddress();
        long netIP = (((long)bAddr[0]) & 0xff) +
                     ((((long)bAddr[1]) & 0xff) << 8) +
                     ((((long)bAddr[2]) & 0xff) << 16) +
                     (((long)bAddr[3] & 0xff) << 24);
        return netIP;
    }

    public void bind(String host, int port) throws Exception {
        InetSocketAddress sockAddress = new InetSocketAddress(host, port);
        dgs = new DatagramSocket(sockAddress);
        dgs.setBroadcast(true);
        dgs.setReuseAddress(true);
        System.out.println("Serving UDP Socket on " + host + " port " + port + " ...");
    }

    public final String receive() throws IOException {
        byte[] buffer = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        dgs.receive(packet);
        System.out.println(packet.getAddress());
        String info = new String(packet.getData(), 0, packet.getLength());
        //System.out.println("Receive: " + info);
        ByteArrayInputStream baos = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(baos);
        try {
            @SuppressWarnings("unchecked")
            Map<Integer,NetTable[]> map = (HashMap<Integer,NetTable[]>) ois.readObject();
            Iterator<Map.Entry<Integer,NetTable[]>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Integer,NetTable[]> entry = entries.next();
                if (!netable.containsKey(entry.getKey())) {
                    netable.put(entry.getKey(), new NetTable[MAXNUM]);
                }
                for (int i=0;i<entry.getValue().length;i++) {
                    NetTable nt = entry.getValue()[i];
                    if (nt != null)
                        update(entry.getKey(), i, nt.getIp());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public void clientSend(String host) {
        //DatagramSocket csock = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            //csock = new DatagramSocket();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(netable);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buffer = baos.toByteArray();
        try {
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length,
                             InetAddress.getByName(host), PORT);
            //csock.send(dgp);
            dgs.send(dgp);
        } catch (IOException e) {
            e.printStackTrace();
        }/* finally {
            try {
                csock.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }*/
    }

    public final void close() {
        try {
            dgs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Map<String,List<String>> getIPList() {
        Map<String, List<String>> net = new HashMap<String, List<String>>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;

            while (networkInterfaces.hasMoreElements()) {
                List<String> ipList = new ArrayList<String>();
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) {
                        System.out.println(Arrays.toString(inetAddress.getAddress()));
                        ipList.add(inetAddress.getHostAddress());
                    }
                }
                net.put(networkInterface.getName(), ipList);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return net;
    }

    public void update(int id, int idx, String value) {
        if (idx >= MAXNUM) {
            System.out.println("The index '" + idx +"' cannot be bigger than MAXNUM '" + MAXNUM + "'");
            return;
        }

        NetTable[] list = netable.get(id);
        if (list[idx] == null) {
            list[idx] = new NetTable(value, true);
        } else if (!value.equals(list[idx].getIp())) {
            // update table
            list[idx].setIp(value);
        }
    }

    public void check(String[] names, boolean flag) {
        Map<String,List<String>> net = NetServer.getIPList();
        net.remove("lo");

        if (flag) {
            for (Map.Entry<Integer,NetTable[]> entry : netable.entrySet())
                System.out.println(entry.getKey() + ":" + Arrays.toString(entry.getValue()));
        }
        for (int i=0; i<names.length; i++) {
            String ip = "";
            String name = names[i];
            List<String> ips = net.get(name);
            if (ips == null || ips.size() != 1) {
                if (ips == null)
                    System.out.println("Interface " + name + " does not exists");
                else
                    System.out.println("Interface '" + name + "' must be only one address");

                if (flag) {
                    System.exit(-1);
                }
            } else {
                ip = ips.get(0);
            }
            update(id, i, ip);
        }
    }

    public static void main(String[] args) {
        final NetServer ns = new NetServer();
        if (args.length < 3) {
            System.out.printf("Usage:  %s <ID> <BCAST> <NIC-NAME>...\n", NetServer.class.getName());
            System.out.println("\t<ID>\t\tHost unique number.");
            System.out.println("\t<BCAST>\tLocal broadcast address.");
            System.out.println("\t<NIC-NAME>\tMultilink interface.");
            System.exit(-1);
        }

        try {
            id = Integer.parseInt(args[0]);
            if (id < 0) throw new NumberFormatException();
        } catch (NumberFormatException ne) {
            System.out.printf("The two parameter '%s' must be valid +number", args[0]);
            System.out.println();
            System.exit(-1);
        }

        netable.put(id, new NetTable[MAXNUM]);
        final String broadcast = args[1];
        final String[] fargs = Arrays.copyOfRange(args, 2, args.length);
        ns.check(fargs, true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ns.clientSend(broadcast);
                ns.check(fargs, false);
            }
        }, 1000, 5000);

        try {
            ns.bind("0.0.0.0", PORT);
        } catch (Exception e) {
            ns.close();
            e.printStackTrace();
            System.exit(-1);
        }

        while (true) {
            try {
                ns.receive();
            } catch (Exception e) {
                ns.close();
                e.printStackTrace();
            }
        }
    }
}
