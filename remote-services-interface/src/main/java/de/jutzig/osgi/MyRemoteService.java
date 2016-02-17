package de.jutzig.osgi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MyRemoteService  extends Remote
{
    public String[] getTestOutput(String[] inputParameters)  throws RemoteException;
}



