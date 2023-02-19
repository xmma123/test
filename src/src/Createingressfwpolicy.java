

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
public class Createingressfwpolicy   extends myvspklib {

    private int Startidx;
    private int Range;


	   public Createingressfwpolicy(int indx,int ranges) throws RestException {
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
	        
	        RedirectionTarget myRT = createredirectiontarget("test",mydomain,"zone0","subnet1","vport1");
	        
	        IngressAdvFwdTemplate myingressAFWTemplate = createingressfwtemplate("usrcontrolroute",mydomain);
	        	        

	        for (int destport = 22;destport <=61; destport++)
	        {
	        	 createingressfwentrytemplate(myingressAFWTemplate, String.valueOf(destport),myRT);
	        }
	        
	         System.out.println("Create usecontrol route for domain: " + mydomain.getName() + " done");
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
	    	   new Thread(new Createingressfwpolicy((threadid * singlecount + 1),singlecount)).start();
	        }
	    	} catch (Exception e){
	        	throw new RuntimeException("Thread end!!!" + e.toString());
	        } 
	    }


    
}