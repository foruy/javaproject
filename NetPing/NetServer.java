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
import java.net.UnknownHostException;
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
    private String addr;
    private boolean valid;

    public NetTable(String addr, boolean valid) {
        this.addr = addr;
        this.valid = valid;
    }

    public String getAddr() {
        return addr;
    }

    public boolean isValid() {
        return valid;
    }

    public void setAddr(String addr) {
        this.addr= addr;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String toString() {
        return addr;
    }
}


public class NetServer {
    private static int id = -1;
    private static int MAXNUM = 0;
    private static final int PORT = 1234;
    private static final int TIMEOUT = 2;
    private DatagramSocket dgs = null;
    private static Map<Integer,Map<Integer,NetTable>> netable = new HashMap<Integer,Map<Integer,NetTable>>();

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
        System.out.println("Packet from>> " + packet.getAddress());
        String info = new String(packet.getData(), 0, packet.getLength());
        //System.out.println("Receive: " + info);
        ByteArrayInputStream baos = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(baos);
        try {
            @SuppressWarnings("unchecked")
            Map<Integer,Map<Integer,NetTable>> map = (Map<Integer,Map<Integer,NetTable>>) ois.readObject();
            Iterator<Map.Entry<Integer,Map<Integer,NetTable>>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Integer,Map<Integer,NetTable>> entry = entries.next();
                if (!netable.containsKey(entry.getKey())) {
                    //netable.put(entry.getKey(), new NetTable[MAXNUM]);
                    netable.put(entry.getKey(), entry.getValue());
                } else {
                    for (Map.Entry<Integer,NetTable> ntMap : entry.getValue().entrySet()) {
                        update(entry.getKey(), ntMap.getKey(), ntMap.getValue().getAddr());
                    }
                    /*for (int i=0;i<entry.getValue().length;i++) {
                        NetTable nt = entry.getValue()[i];
                        if (nt != null)
                            update(entry.getKey(), i, nt.getIp());
                    }*/
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

            while (networkInterfaces.hasMoreElements()) {
                List<String> ipList = new ArrayList<String>();
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) {
                        //System.out.println(Arrays.toString(inetAddress.getAddress()));
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

        Map<Integer,NetTable> ntMap = netable.get(id);
        if (ntMap != null) {
            NetTable nt = ntMap.get(idx);
            if (nt == null) {
                nt = new NetTable(value, "".equals(value) ? false : true);
            } else if (!value.equals(nt.getAddr())) {
                // update kernel
                nt.setAddr(value);
            }
        }

        /*NetTable[] list = netable.get(id);
        if (list[idx] == null) {
            list[idx] = new NetTable(value, true);
        } else if (!value.equals(list[idx].getIp())) {
            // update table
            list[idx].setIp(value);
        }*/
    }

    protected void ping() {
        for (Map.Entry<Integer,Map<Integer,NetTable>> entries : netable.entrySet()) {
            /*if (entries.getKey() == id) {
                continue;
            }*/

            for (Map.Entry<Integer,NetTable> entry : entries.getValue().entrySet()) {
                NetTable nt = entry.getValue();
                if (!"".equals(nt.getAddr())) {
                    boolean stat = false;
                    try {
                        stat = InetAddress.getByName(nt.getAddr()).isReachable(TIMEOUT);
                        System.out.printf("Status>> %s/%s\n", nt.getAddr(), stat ? "\033[32mActive\033[0m" : "\033[31mDown\033[0m");
                    } catch (UnknownHostException ue) {
                        System.out.printf("unknown host: %s\n", nt.getAddr());
                    } catch (IOException ie) {
                    }

                    if (stat != nt.isValid()) {
                        // update kernel
                        nt.setValid(stat);
                    }
                }
            }
        }
    }

    public void checkLocal(String[] names, boolean flag) {
        Map<String,List<String>> net = NetServer.getIPList();
        net.remove("lo");

        if (!flag) {
            for (Map.Entry<Integer,Map<Integer,NetTable>> entry : netable.entrySet())
                System.out.println("Network Table>> " + entry.getKey() + ":" + entry.getValue());
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

        final String broadcast = args[1];
        final String[] fargs = Arrays.copyOfRange(args, 2, args.length);
        MAXNUM = fargs.length;
        Map<Integer,NetTable> nt = new HashMap<Integer,NetTable>();
        for (int i=0;i<MAXNUM;i++) {
            nt.put(i, new NetTable("", false));
        }
        netable.put(id, nt);
        //netable.put(id, new NetTable[MAXNUM]{new NetTable("", false)});
        ns.checkLocal(fargs, true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ns.clientSend(broadcast);
                ns.checkLocal(fargs, false);
                ns.ping();
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
