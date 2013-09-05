package jp.co.opentone.fsol.ishii.html5;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

@WebServlet(value = { "/echo" })
public class EchoServlet extends WebSocketServlet {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6946416208261279049L;

    /**
     * スレッドセーフなIDカウンタ
     */
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    /**
     * スレッドセーフなEchoInboundのSet
     */
    private static final Set<MessageInbound> echoInboundSet = new CopyOnWriteArraySet<MessageInbound>();

    @Override
    protected StreamInbound createWebSocketInbound(String subProtocol,
            HttpServletRequest request) {
        return new EchoInbound(idCounter.getAndIncrement());
    }

    /**
     * 全体にEchoするWebSocket
     */
    private class EchoInbound extends MessageInbound {

        private String id;

        public EchoInbound(int id) {
            this.id = Integer.toString(id);
        }

        @Override
        protected void onOpen(WsOutbound outbound) {
            System.out.println("onOpen");
            echoInboundSet.add(this);

            try {
                outbound.writeTextMessage(CharBuffer.wrap("YOU ARE " + id));
                talk("JOIN " + id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onClose(int status) {
            System.out.println("onClose(status = " + status + ")");
            echoInboundSet.remove(this);
            try {
                talk("PART " + id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onBinaryMessage(ByteBuffer message) throws IOException {
            System.out.println("onBinaryMessage");
        }

        @Override
        protected void onTextMessage(CharBuffer message) throws IOException {
            talk(id + "> " + message);
        }

        private void talk(String message) throws IOException{
            CharBuffer charBuffer = CharBuffer.wrap(message);
            for (MessageInbound echoInbound : echoInboundSet) {
                echoInbound.getWsOutbound().writeTextMessage(charBuffer);
                charBuffer.position(0);
            }
        }
    }
}
