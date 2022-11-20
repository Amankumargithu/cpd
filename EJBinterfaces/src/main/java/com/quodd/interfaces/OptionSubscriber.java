
package com.quodd.interfaces;

import javax.ejb.Remote;
@Remote
public interface OptionSubscriber
{
   public void subscribe(String id, Object[] keys);

}

