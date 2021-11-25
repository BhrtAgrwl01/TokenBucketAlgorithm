package week2;
import java.util.*;
import java.util.concurrent.*;

class TokenBucket{
	
	 int maxqueue = 10;
	 int returnedP = 0;
	 int transmittedP = 0;
	 int cycle = 0;
     Queue<TokenBucket.Packet> queue = new PriorityQueue<TokenBucket.Packet>();
     Object lock1 = new Object();
     ArrayList<ExtDevice> devicelist = new ArrayList<ExtDevice>();
	
	
	void beginprocess(int numExtDevice, int tokenAssigned){
		
		IntDeviceBuilder builder = new IntDeviceBuilder().setRate(2).setCurrent(tokenAssigned).setMax(10);	
		IntDevice testrun = builder.getIntDevice();
		for(int i=0;i<numExtDevice;i++) {
			ExtDeviceBuilder builder1 = new ExtDeviceBuilder().setRate(2).setID(i+1);		
			devicelist.add(builder1.getExtDevice());
		}

	     Runnable helloRunnable = new Runnable() {
	    	    public void run() {
	   	    	 testrun.transmission();
	    	    }
	    	};
	    	
	    	for(int i=0;i<numExtDevice;i++) {
				devicelist.get(i).start();
			}

	    	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	    	executor.scheduleAtFixedRate(helloRunnable, 1, 1, TimeUnit.SECONDS);
	    	 
	}
	
	class ExtDeviceBuilder {
		int rate_trans;
		int id;
		
		ExtDeviceBuilder setRate(int r) {
			this.rate_trans = r;
			return this;
		}
		ExtDeviceBuilder setID(int id) {
			this.id = id;
			return this;
		}
		ExtDevice getExtDevice() {
	    	return new ExtDevice(rate_trans,id);
	    }
	}
	
	class ExtDevice extends Thread {
	    int rate_trans;
	    private int id;
	    int numtrans;
	    int numret;
	    int numcurr;
	    int extcycle=-1;
	    int absnum=0;

	    ExtDevice(int rate, int id) {
	        rate_trans = rate;
	        this.id = id;
	    }

	    int getID() {
	        return this.id;
	    }

	    public void run() {
	        synchronized (lock1) {
	                while(true) {
	                	try {
		                    if(cycle==extcycle + 1) {
		                    	numcurr = rate_trans;
		                    	extcycle++;
		                    }
		                    while (numcurr > 0) {
		                    	numtrans++;
		                    	absnum++;
		                    	Packet temp = new Packet(this, absnum);
		                        if (queue.size() < maxqueue) {
		                            queue.add(temp);

		                        } else {
		                        	temp.packetReturned();
		                        	numret++;
		                        }
		                        numcurr--;
		                        
		                    }   
		                } catch (Exception e) {
		                	e.printStackTrace();
		                    System.out.println("Exception caught");
		                }	
	                	try {
		                    lock1.wait(500);
		                } catch (InterruptedException e) {
		                    e.printStackTrace();
		                }
		              }
	                }
	    }

	}
	
	class IntDeviceBuilder {
		int ratetoken;
	    int currenttoken;
	    int maxtoken;
	    
	    IntDeviceBuilder setRate(int r) {
	    	this.ratetoken = r;
	    	return this;
	    }
	    IntDeviceBuilder setCurrent(int r) {
	    	this.currenttoken = r;
	    	return this;
	    }
	    IntDeviceBuilder setMax(int r) {
	    	this.maxtoken = r;
	    	return this;
	    }
	    
	    IntDevice getIntDevice() {
	    	return new IntDevice(ratetoken,currenttoken,maxtoken);
	    }
	    
	}

	class IntDevice {
	    int ratetoken;
	    int currenttoken;
	    int maxtoken;

	    IntDevice(int tokrate, int tokcur, int tokmax) {
	        this.ratetoken = tokrate;
	        this.currenttoken = tokcur;
	        this.maxtoken = tokmax;
	    }
	    void transmission() {
	    	
	        currenttoken += ratetoken;
	        if (currenttoken > maxtoken)
	            currenttoken = maxtoken;
	        while (currenttoken > 0 && queue.size() > 0) {
	            queue.poll().packetTransmitted();
	            currenttoken--;
	        }
	        cycle++;	     
	        System.out.println("____");
	    }
	}

	class Packet implements Comparable<Packet> {
	    int source;
	    int num;

	    Packet(ExtDevice ed, int a) {
	        this.source = ed.getID();
	        this.num = a;
	    }

	    void packetTransmitted() {
	        System.out.println("Packet " + num + " from source " + source + " transmitted.");
	    }
	    
	    void packetReturned() {
	    	System.out.println("Packet " + num + " from source " + source + " returned.");
	    }

	    @Override
	    public int compareTo(Packet obj) {
	        return this.num - obj.num;
	    }

	}
}

class TokenAlgo {
    public static void main(String args[]) throws InterruptedException {
    	
        TokenBucket tb = new TokenBucket();
        
        Scanner in = new Scanner(System.in);
        System.out.print("Enter number of External Devices: ");
        
        int numExtDevice = in.nextInt();
        System.out.print("Enter initial tokens present in bucket: ");
        int tokenAssigned = in.nextInt();
        
        tb.beginprocess(numExtDevice, tokenAssigned);
        
        long start = System.currentTimeMillis();
        while(true) {
        	if(System.currentTimeMillis()-start>8000) {
        		
        		for(int i=0;i<numExtDevice;i++) {
    				tb.returnedP += tb.devicelist.get(i).numret;
    				tb.transmittedP += tb.devicelist.get(i).numtrans;
    			}
        		
             System.out.println("Number of packets returned : "+tb.returnedP);
             System.out.println("Number of packets transmitted : "+(tb.transmittedP-tb.returnedP-tb.queue.size()));
             System.out.println("EXITED");
   	    	 System.exit(0);
   	     }
        }
    }
}
