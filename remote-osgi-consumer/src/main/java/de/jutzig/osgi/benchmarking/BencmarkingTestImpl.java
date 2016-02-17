package de.jutzig.osgi.benchmarking;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jutzig.osgi.MyRemoteService;


@Command(scope = "benchmark", name = "run", description="Runs an osgi remote benchmark")
@Service
public class BencmarkingTestImpl implements Action
{
    private static final Logger logger = LoggerFactory.getLogger(BencmarkingTestImpl.class);

    private static final String[] input = new String[10];

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static Random rnd = new Random();

    @Argument(index = 0, name = "invocations", description = "The amount sequential invocations", required = false, multiValued = false)
    String invocations = "1";

    @Argument(index = 1, name = "parallel", description = "The amount of threads to use", required = false, multiValued = false)
    String parallel = "1";


    @Argument(index = 2, name = "rmihost", description = "The host for rmi benchmark (optional)", required = false, multiValued = false)
    String rmihost = null;


    @Reference
    protected MyRemoteService mrs;

    private static String randomString(int len)
    {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    static
    {
        for (int i = 0; i < 10; i++)
        {
            input[i] = randomString(160);
        }
    }


    public void runRemoteOSGiTestParallel(final long testCalls, int parallelLevel) throws RemoteException
    {
        for (int i = 0; i < 100; i++)
        {
            mrs.getTestOutput(input);
        }

        try
        {
            runParallelTest(new Runnable()
            {

                public void run()
                {
                    for (int i = 0; i < testCalls; i++)
                    {
                        try
                        {
                            mrs.getTestOutput(input);
                        }
                        catch (RemoteException e)
                        {
                            logger.error("Remote call failed", e);
                        }
                    }
                }
            }, parallelLevel, "parallel remote OSGi");
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void runRMITestParallel(final long testCalls, int parallelLevel)
    {

        try {
            Registry registry = LocateRegistry.getRegistry(rmihost,30000);
            final MyRemoteService stub = (MyRemoteService) registry.lookup("MyRemoteService");
            runParallelTest(new Runnable()
            {

                public void run()
                {
                    for (int i = 0; i < testCalls; i++)
                    {
                        try
                        {
                            stub.getTestOutput(input);
                        }
                        catch (RemoteException e)
                        {
                            logger.error("Remote call failed",e);
                        }
                    }
                }
            }, parallelLevel, "parallel RMI");
        } catch (Exception e) {
            logger.error("Remote call failed",e);
        }
    }


    private void runParallelTest(Runnable worker, int parallelLevel, String testName) throws InterruptedException
    {
        ExecutorService fixedPoolExecutor = Executors.newFixedThreadPool(parallelLevel);
        long start = System.nanoTime();
        for (int i = 0; i < parallelLevel; i++)
        {
            fixedPoolExecutor.execute(worker);
        }

        fixedPoolExecutor.shutdown();
        fixedPoolExecutor.awaitTermination(2, TimeUnit.MINUTES);
        long end = System.nanoTime();
        long total = (end - start) / 1000000;
        System.out.println("Test " + testName + " completed in " + total + " milliseconds. Parallel level is " + parallelLevel);
        logger.info("Test " + testName + " completed in " + total + " milliseconds. Parallel level is " + parallelLevel);
    }

    @Override
    public Object execute() throws Exception
    {
        int inv = Integer.parseInt(invocations);
        int par = Integer.parseInt(parallel);
        runRemoteOSGiTestParallel(inv, par);
        if(rmihost!=null)
        {
            runRMITestParallel(inv, par);
        }
        return null;
    }
}
