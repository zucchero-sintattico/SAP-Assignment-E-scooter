package distributedlogservice;

import distributedlogservice.infrastructure.DistributedLogController;

public class DistributedLogService {

    private static final int PORT = 9003;
    int restAPIPort;
    
    public DistributedLogService() {
        restAPIPort = PORT;
    }
    
    public void launch() {
        DistributedLogController restBasedAdapter = new DistributedLogController(restAPIPort);
        restBasedAdapter.init();
    }

}
