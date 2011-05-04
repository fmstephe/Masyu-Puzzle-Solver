package org.francis.intel.challenge;

import org.francis.intel.challenge.search.MasyuSearcher;
import org.francis.p2p.worksharing.network.Communicator;
import org.francis.p2p.worksharing.network.NetworkManager;
import org.francis.p2p.worksharing.smp.SMPCommunicator;
import org.francis.p2p.worksharing.smp.SMPMessageManager;
import org.francis.p2p.worksharing.smp.SMPWorkerId;

public class SMPThreadedMasyuSolverFactory {
    
    private SMPMessageManager messageManager;
    int height;
    int width;
    int[] board;
    
    public SMPThreadedMasyuSolverFactory(int height, int width, int[] board) {
        this.height = height;
        this.width = width;
        this.board = board;
    }
    
    public SMPMessageManager createAndRunSolversLocal(int networkSize, int worksharingThreshold, String logFilePath) {
        NetworkManager[] networkManagers = createNetworkManagers(networkSize, worksharingThreshold, networkSize, logFilePath);
        MasyuSearcher[] rSolvers = createRunnableSolvers(networkManagers);
        setThoseBastardsRunning(rSolvers);
        return messageManager; // The contract between a caller who provides a listener and the callee that provides a message-manager for that listener is fucking tenuous and non-intuitive.  Bullshit code.
    }
    
    private void setThoseBastardsRunning(MasyuSearcher[] rSolvers) {
        for (int i = 0; i < rSolvers.length; i++) {
            MasyuSearcher rSolver = rSolvers[i];
            Thread thread = new Thread(rSolver);
            thread.setName("Solver_"+i);
            thread.start();
        }
    }

    private MasyuSearcher[] createRunnableSolvers(NetworkManager[] networkManagers) {
        MasyuSearcher[] searchers = new MasyuSearcher[networkManagers.length];
        for (int i = 0; i < networkManagers.length; i++) {
            NetworkManager nwm = networkManagers[i];
            searchers[i] = new MasyuSearcher(height, width, board, nwm, (i==0));
        }
        return searchers;
    }
    
    private NetworkManager[] createNetworkManagers(int networkSize, int workSharingThreshold, int threadCount, String logFilePath) {
        NetworkManager[] networkManagers = new NetworkManager[threadCount];
        SMPWorkerId[] workers = new SMPWorkerId[threadCount];
        for (int i = 0; i < threadCount; i++) {
            workers[i] = new SMPWorkerId(i);
        }
        messageManager = new SMPMessageManager(workers);
        if (threadCount == 1) {
            Communicator comm = new SMPCommunicator(messageManager,null,null,workers[0]);
            networkManagers[0] = new NetworkManager(comm,networkSize,workSharingThreshold,logFilePath);
        }
        else {
            for (int i = 0; i < threadCount; i++) {
                Communicator comm = null;
                if (i == 0)
                    comm = new SMPCommunicator(messageManager,null,workers[i+1],workers[i]);
                else if (i == threadCount-1)
                    comm = new SMPCommunicator(messageManager,workers[i-1],null,workers[i]);
                else
                    comm = new SMPCommunicator(messageManager,workers[i-1],workers[i+1],workers[i]);
                networkManagers[i] = new NetworkManager(comm,networkSize,workSharingThreshold,logFilePath);
            }
        }
        return networkManagers;
    }
}
