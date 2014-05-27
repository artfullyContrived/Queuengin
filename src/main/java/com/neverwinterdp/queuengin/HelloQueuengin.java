package com.neverwinterdp.queuengin;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.message.SampleEvent;
import com.neverwinterdp.queuengin.kafka.KafkaMessageConsumerConnector;
import com.neverwinterdp.queuengin.kafka.KafkaMessageProducer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class HelloQueuengin {
  static public class Options {
    @Parameter(
      names = "-produce", 
      description = "Produce the messages"
    )
    boolean produce = true ; 
    
    @Parameter(
        names = "-consume", 
        description = "Consume the messages"
    )
    boolean consume = true ;
    
    @Parameter(
        names = "-topic", 
        description = "topic name"
    )
    String topic = "HelloQueuengin";
    
    @Parameter(
       names = "-zk-connect", 
       description = "The list of the zookeeper server in fomat host:port, separate by comma"
    )
    String zkConnect = "127.0.0.1:2181";
    
    @Parameter(
        names = "-kafka-connect", 
        description = "The list of the kafka server in fomat host:port, separate by comma"
    )
    String kafkaConnect = "127.0.0.1:9092";
    
    @Parameter(
        names = "-num-message", 
        description = "The number of messages to be produced"
    )
    int numMessage = 50000;
  }
  
  static public class HelloProducer implements Runnable {
    Options opts ;
    int     count ;
    HelloProducer(Options options) {
      this.opts = options ;
    }
    
    public void run() {
      try {
        KafkaMessageProducer producer = new KafkaMessageProducer(opts.kafkaConnect) ;
        for(int i = 0 ; i < opts.numMessage; i++) {
          SampleEvent event = new SampleEvent("event-" + i, "Hello Queuengin " + i) ;
          Message jsonMessage = new Message("m" + i, event, false) ;
          producer.send(opts.topic,  jsonMessage) ;
          count++ ;
        }
        System.out.println("Produce " + opts.numMessage + " messages");
        producer.close();
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
  static public class HelloMessageConsumerHandler implements MessageConsumerHandler {
    int count =  0 ;
    Options opts ;
    
    HelloMessageConsumerHandler(Options opts) {
      this.opts = opts ;
    }
    
    public void onMessage(Message jsonMessage) {
      count++ ;
      try {
        SampleEvent event = jsonMessage.getData().getDataAs(SampleEvent.class);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public void onErrorMessage(Message message, Throwable error) {
    }
    
    public void close() {
      System.out.println("Consume " + count + " messages");
    }
  }
  
  static public void main(String[] args) throws Exception {
    Options options = new Options();
    JCommander parser =new JCommander(options, args);
    parser.usage() ;
    
    Thread producerThread = null ;
    HelloProducer helloProducer = null ;
    if(options.produce) {
      helloProducer = new HelloProducer(options) ;
      producerThread = new Thread(helloProducer) ;
      producerThread.start(); 
    }
    
    HelloMessageConsumerHandler  handler = new HelloMessageConsumerHandler(options) ;
    KafkaMessageConsumerConnector consumer = 
        new KafkaMessageConsumerConnector("consumer", options.zkConnect) ;
    consumer.consume(options.topic, handler, 1) ;
    
    int lastConsume = -1;
    long startTime = System.currentTimeMillis() ;
    while(true) {
      Thread.sleep(1000);
      int produce = helloProducer != null ? helloProducer.count : 0 ;
      int consume = handler != null ? handler.count : 0 ;
      System.out.println("Produce = " + produce + ", Consume = " + consume + " in " + (System.currentTimeMillis() - startTime) + "ms");
      if(lastConsume == consume) break ;
      lastConsume = consume ;
    }
    
    handler.close(); 
    consumer.close();
    System.exit(0);
  }
}