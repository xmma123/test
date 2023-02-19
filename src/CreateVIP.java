

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import net.nuagenetworks.bambou.RestException;
import net.nuagenetworks.vspk.v6.*;
import net.nuagenetworks.vspk.v6.fetchers.*;



/**
 * Fetches all existing VSD Enterprise objects
 * 
 * Precondition - requires a running VSD server at port matching MY_VSD_SERVER_PORT
 */
public class CreateVIP   extends myvspklib {

    private int Startidx;
    private int Range;


	   public CreateVIP(int indx,int ranges) throws RestException {
		    super();
	    	this.Startidx=indx;
	    	this.Range=ranges;
		    
		// TODO Auto-generated constructor stub
	}

	    @Override
	    public void run() {
	        System.out.println("Fetching my Enterprises");
	        try {

	    		   session = new VSDSession("csproot", "csproot", "csp", MY_VSD_SERVER_PORT);
	    	       session.getClientTemplate().disableCertificateValidation();
	    		   session.start();   

	        Enterprise myenterprise = fetchEnterprisebyName("mypc");
	        for (int vpccounter = Startidx; vpccounter < Startidx + Range ; vpccounter ++) {
	        String vpcname = "VPC-" + String.valueOf(vpccounter);        
	        Domain mydomain = fetchDomainbyName(vpcname,myenterprise);
	        if ( mydomain == null) {
	        	System.out.println("Not Found Enterprise: " + myenterprise.getName() + " domain: " + vpcname);
	        	continue;
	        }

	        Subnet mysubnet = fetchSubnetbydomainzone("subnet1",mydomain,"zone0");
	        
	         System.out.println("Found Enterprise: " + myenterprise.getName() + " domain: " + mydomain.getName() + " subnet: " + mysubnet.getName());
	         VPort myvport=fetchVPortByNameForSubnet("vport1",mysubnet);
	         VirtualIP myvip = createVIPforVPort("10.40.18.2",myvport);
	         System.out.println("Create VIP: " + myvip.getVirtualIP() + "  for Vport : " + myvport.getName());
	        } 
	         System.out.println("Thread" + this.toString() + " : run completely ");
	        } catch (Exception e){
	        	throw new RuntimeException("Thread end!!!" + e.toString());
	        }    
	    }
	    
	    public static void main(String[] args) {
	    	int allcount=10000;
	    	int singlecount=2000;
	    	int threads=allcount/singlecount;
	    	try {
	        for (int threadid = 0; threadid < threads; threadid ++) 
	        {
	    	   new Thread(new CreateVIP((threadid * singlecount + 1),singlecount)).start();
	        }
	    	} catch (Exception e){
	        	throw new RuntimeException("Thread end!!!" + e.toString());
	        } 
	    }


    
}