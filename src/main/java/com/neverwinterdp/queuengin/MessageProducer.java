package com.neverwinterdp.queuengin;

import java.util.List;

import com.neverwinterdp.message.Message;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public interface MessageProducer {
  public String getName() ;
  public void   setName(String name) ;
  public void send(String topic, Message msg) throws Exception ;
  public void send(String topic, List<Message> messages) throws Exception ;
  public void close() ;
}
