package org.openTwoFactor.server.loadTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TfSourceUtils;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


public class LoadtestSubjectSource {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    Thread[] threads = new Thread[5]; 
    
    for (int i=0;i<5;i++) {
      
      threads[0] = new Thread(new Runnable() {

        @Override
        public void run() {
          
          try {
            loadTestSubjectSource();
          } catch (RuntimeException re) {
            re.printStackTrace();
            throw re;
          }
          
          
        }
        
      });
      
      threads[0].start();
    }
    

  }

  
  public static void loadTestSubjectSource() {
    
    //get a bunch
    Source source = TfSourceUtils.mainSource();
    
    Set<Subject> subjects = TfSourceUtils.search(source, "smith", false); 
    
    List<Subject> subjectList = new ArrayList<Subject>(subjects);
    
    String netIdAttribute = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.ws.log.requestsResponsesLogSubjectAttribute");
    
//    for (int i=0;i<subjects.size();i++) {
//      System.out.println(i + ". " + subjectList.get(i).getDescription());
//    }

    for (int i=0;i<500;i++) {
      
      int index = new Random().nextInt(subjectList.size());
      Subject subject = subjectList.get(index);

      String subjectIdOrIdentifier = i%2==0 ? subject.getId() : subject.getAttributeValue(netIdAttribute);
      
      subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(source, subjectIdOrIdentifier, false, false, false);
      
      if (subject == null) {
        System.out.println("subject is null: " + subjectIdOrIdentifier);
      }
      
      if ((i+1)%100==0) {
        System.out.println(Thread.currentThread().getName() + ": " + i + ": records");
      }
     
      if ((i+1)%10==0 && subject.getDescription().length() > 3) {

        //do a search 
        Set<Subject> subjectResults = TfSourceUtils.searchPage(source, subject.getDescription().substring(0,3), true);
        System.out.println("Searched for: " + subject.getDescription().substring(0,3) + ", " + subjectResults.size());
      
      }
      
    }
    
  }
  
}
