package micro;

public class Address {
    private byte[] ip;
    private int port;

    public Address(byte[] ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public byte[] getIp() {
        return ip;
    }

    public void setIp(byte[] ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
