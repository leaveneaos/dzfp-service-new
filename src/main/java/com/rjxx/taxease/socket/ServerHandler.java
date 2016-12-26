package com.rjxx.taxease.socket;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2016/10/24.
 */
public class ServerHandler extends IoHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private static Map<String, IoSession> cachedSession = new ConcurrentHashMap<>();

    private static Map<String, SocketRequest> cachedRequestMap = new ConcurrentHashMap<>();

    static {
        Timer clearRequestTimer = new Timer();
        ClearRequestTask clearRequestTask = new ClearRequestTask();
        clearRequestTask.setMap(cachedRequestMap);
        clearRequestTimer.schedule(clearRequestTask, 60000, 5000);
    }

    public static String sendMessage(String taxcardno, String command, String params) throws Exception {
        IoSession session = cachedSession.get(taxcardno);
        if (session == null) {
            throw new Exception("客户端：" + taxcardno + "没有连上服务器");
        }
        String commandId = UUID.randomUUID().toString().replace("-", "");
        String sendMessage = command + " " + commandId + " " + Base64.encodeBase64String(params.getBytes("UTF-8"));
        session.write(sendMessage);
        SocketRequest socketRequest = new SocketRequest();
        socketRequest.setCommandId(commandId);
        cachedRequestMap.put(commandId, socketRequest);
        synchronized (socketRequest) {
            socketRequest.wait();
        }
        if (socketRequest.getException() != null) {
            throw socketRequest.getException();
        }
        logger.debug("---" + taxcardno + " return message:" + socketRequest.getReturnMessage());
        return socketRequest.getReturnMessage();
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        session.setAttribute("openTime", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        String taxcardno = (String) session.getAttribute("taxcardno");
        String openTime = (String) session.getAttribute("openTime");
        String closeTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        if (taxcardno != null) {
            logger.info("one session " + taxcardno + " has closed:" + openTime + "_______" + closeTime);
        } else {
            logger.info("unknow session closed:" + openTime + "_______" + closeTime);
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        session.removeAttribute("idleCount");
        String msg = (String) message;
        String[] arr = msg.split(" ");
        String command = arr[0];
        String commandId = "";
        String returnMessage = "";
        if (arr.length > 1) {
            commandId = arr[1];
        }
        if (arr.length > 2) {
            returnMessage = arr[2];
            //base64解码
            returnMessage = new String(Base64.decodeBase64(returnMessage), "UTF-8");
        }
        String cardno = (String) session.getAttribute("taxcardno");
        if (cardno == null && !"login".equals(command)) {
            //假如没有登录过并且不是登录命令
            session.closeNow();
            return;
        }
        if (cardno != null) {
            logger.debug("receive " + cardno + " message:" + msg);
        } else {
            logger.debug("receive message:" + command + " " + commandId + " " + returnMessage);
        }
        if (command.equals("heartbeat")) {
            return;
        } else if (command.equals("login") && arr.length > 1) {
            String taxcardno = arr[1];
            logger.info("-----taxcardno " + taxcardno + " login!!!");
            IoSession tmp = cachedSession.get(taxcardno);
            if (tmp != null) {
                tmp.closeNow();
            }
            cachedSession.put(taxcardno, session);
            session.setAttribute("taxcardno", taxcardno);
            logger.info("current login session count: " + cachedSession.size());
            return;
        } else if (arr.length > 1) {
            SocketRequest socketRequest = cachedRequestMap.remove(commandId);
            if (socketRequest != null) {
                if (arr.length > 2) {
                    socketRequest.setReturnMessage(returnMessage);
                } else {
                    socketRequest.setReturnMessage("");
                }
                synchronized (socketRequest) {
                    socketRequest.notifyAll();
                }
                return;
            } else {
                logger.info("commandId:" + commandId + " not found");
                return;
            }
        }
        //以下都是无效的消息
        logger.info("invalid message:" + msg);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
//        logger.info("message:" + message);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        Integer count = (Integer) session.getAttribute("idleCount");
        if (count == null) {
            session.setAttribute("idleCount", 1);
        } else if (count >= 2) {
            session.closeNow();
            return;
        } else {
            count++;
            session.setAttribute("idleCount", count);
        }
        session.write("heartbeat");
    }
}
