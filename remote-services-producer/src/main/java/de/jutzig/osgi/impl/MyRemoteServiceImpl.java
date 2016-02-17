package de.jutzig.osgi.impl;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import de.jutzig.osgi.MyRemoteService;


@Component(enabled=true,immediate=true)
@Service(value = MyRemoteService.class)
@Properties({@Property(name = "service.exported.interfaces", value = "*"),
    @Property(name="service.exported.configs",value="ecf.generic.server")//),  value={"fabric-dosgi","ecf.r_osgi.peer","ecf.generic.server","org.apache.cxf.ws"})
//    @Property(name="org.apache.cxf.ws.address", value="http://vogts02:9090/myServiceTest")
    })
public class MyRemoteServiceImpl implements MyRemoteService
{

    private static final String[] output = new String[20];

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static Random rnd = new Random();

    private Registry registry;

    @Activate
    public void activate()
    {
        try {

            MyRemoteService stub = (MyRemoteService) UnicastRemoteObject.exportObject(this, 2000);

            // Bind the remote object's stub in the registry
            registry = LocateRegistry.createRegistry(30000);

            registry.bind("MyRemoteService", stub);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate()
    {
        try
        {
            registry.unbind("MyRemoteService");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static String randomString(int len)
    {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    static
    {
        for (int i = 0; i < 20; i++)
        {
            output[i] = randomString(160);
        }
    }

    public String[] getTestOutput(String[] inputParameters)
    {
        return output;
    }
}
