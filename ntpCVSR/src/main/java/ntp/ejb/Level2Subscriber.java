
package ntp.ejb;

import java.util.ArrayList;

import javax.ejb.Remote;
@Remote
public interface Level2Subscriber
{
   public ArrayList<String> subscribe(String id, Object[] keys);
}

