
package com.quodd.interfaces;

import javax.ejb.Remote;
@Remote
public interface TSQSubscriber
{
   public void subscribe(String id, Object[] keys);

}

