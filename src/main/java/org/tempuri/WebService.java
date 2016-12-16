package org.tempuri;

import javax.jws.WebMethod;

/**
 * Created by kzx on 2016/12/15.
 */
public interface WebService {

    @WebMethod
    public String CallQuery(String AppKey, String Secret, String QueryData);

    
}
