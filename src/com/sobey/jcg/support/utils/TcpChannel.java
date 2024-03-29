package com.sobey.jcg.support.utils;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


public final class TcpChannel{
    private long endTime;
    private SelectionKey key;
    private boolean closed = false;
    public TcpChannel(SelectableChannel channel, int outTime, int op) throws IOException{
        boolean done = false;
        Selector selector = null;
        updateTimeout(outTime);
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            key = channel.register(selector, op);
            done = true;
        } finally {
            if (!done && selector != null) {
                selector.close();
            }
            if (!done) {
                channel.close();
            }
        }
    }

    
    public static void blockUntil(SelectionKey key, long endTime) throws IOException{
        long timeout = endTime - System.currentTimeMillis();
        int nkeys = 0;
        if (timeout > 0) {
            nkeys = key.selector().select(timeout);
        } else if (timeout == 0) {
            nkeys = key.selector().selectNow();
        }
        if (nkeys == 0) {
            throw new SocketTimeoutException();
        }
    }

    public void cleanup() {
        try {
            key.selector().close();
            key.channel().close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        closed = true;
    }

    public boolean isClosed(){
        return closed;
    }

    
    public void updateTimeout(long time) {
        endTime = System.currentTimeMillis() + time;
    }

    public String toString(){
        return key.channel().toString();
    }

    public void bind(SocketAddress addr) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.socket().bind(addr);
    }

    public void connect(SocketAddress addr) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        key.interestOps(key.interestOps() | SelectionKey.OP_CONNECT);
        try {
            if (!key.isConnectable()) {
                blockUntil(key, endTime);
            }
            if (!channel.connect(addr) && !channel.finishConnect()) {
                throw new ConnectException();
            }
        } finally {
            if (key.isValid()) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
            }
        }
    }

    public void send(ByteBuffer buffer) throws IOException {
        Send.operate(key, buffer, endTime);
    }

    public void recv(ByteBuffer buffer) throws IOException {
        Recv.operate(key, buffer, endTime);
    }
}

interface Operator{
    class Operation {
        static void operate(final int op, final SelectionKey key, final ByteBuffer buffer, final long endTime, final Operator optr) throws IOException {
            final SocketChannel channel = (SocketChannel) key.channel();
            final int total = buffer.capacity();
            key.interestOps(op);
            try {
                while (buffer.position() < total) {
                    if (System.currentTimeMillis() > endTime) {
                        throw new SocketTimeoutException();
                    }
                    if ((key.readyOps() & op) != 0) {
                        if (optr.io(channel, buffer) < 0) {
                            throw new EOFException();
                        }
                    } else {
                        TcpChannel.blockUntil(key, endTime);
                    }
                }
            } finally {
                if (key.isValid()) {
                    key.interestOps(0);
                }
            }
        }
    }

    int io(SocketChannel channel, ByteBuffer buffer) throws IOException;
}

class Send implements Operator {
    public int io(SocketChannel channel, ByteBuffer buffer) throws IOException {
        return channel.write(buffer);
    }

    public static final void operate(final SelectionKey key, final ByteBuffer buffer, final long endTime) throws IOException {
        Operation.operate(SelectionKey.OP_WRITE, key, buffer, endTime, operator);
    }

    public static final Send operator = new Send();
}

class Recv implements Operator{
    public int io(SocketChannel channel, ByteBuffer buffer) throws IOException{
        return channel.read(buffer);
    }

    public static final void operate(final SelectionKey key, final ByteBuffer buffer, final long endTime) throws IOException {
        Operation.operate(SelectionKey.OP_READ, key, buffer, endTime, operator);
    }
    public static final Recv operator = new Recv();
}