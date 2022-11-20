
package ntp.ejb;

import javax.ejb.Remote;
@Remote
public interface TSQSubscriber
{
   public void subscribe(String id, Object[] keys);

}

