

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
public class Createdomain implements Runnable {
    private static final String MY_VSD_SERVER_PORT = "https://192.168.56.106:8443";
    private static final VSDSession session;
    private int startidx;
    
    public Createdomain(int indx) {
    	this.startidx=indx;
    }

    static {
        session = new VSDSession("csproot", "csproot", "csp", MY_VSD_SERVER_PORT);
        session.getClientTemplate().disableCertificateValidation();
    }
    @Override
    public void run() {//步骤 2
		//run方法内为具体的逻辑实现
    	System.out.println("Fetching All Enterprises");
    	try {
        session.start();
        Enterprise myenterprise = fetchEnterprisebyName("mypc");
 //       System.out.println("Number of Enterprises found : " + enterprises.size());
        for (long vpccounter = startidx; vpccounter < startidx+5001; vpccounter ++) {
           String vpcname = "VPC-" + String.valueOf(vpccounter);        
           Domain mydomain = createdomain(vpcname,myenterprise);
           if ( mydomain == null) {
            	System.out.println("DOMAIN create failed : " + myenterprise.getName() + " domain: " + vpcname);
        	    continue;
           } else {
        	    System.out.println("DOMAIN create successful : " + myenterprise.getName() + " domain: " + mydomain.getName());
           }
        }
    	}
        catch (Exception e) {
        	
        }
     
    }
    
    
    public static void main(String[] args) {
        for (int threadid =1; threadid<3; threadid ++) 
        {
    	   new Thread(new Createdomain((threadid-1)*5000+1)). start();
        }
    }


    private List<Enterprise> fetchAllEnterprises() throws RestException {
        EnterprisesFetcher fetcher = session.getMe().getEnterprises();
        List<Enterprise> enterprises = fetcher.get();
        return enterprises;
    }
    
    private Enterprise fetchEnterprisebyName(String enterprisename) throws RestException {
    	String filter = String.format("name == '%s'", enterprisename);
    	EnterprisesFetcher fetcher = session.getMe().getEnterprises();
    	Enterprise	myenterprise = fetcher.getFirst(filter, null, null, null, null, null, true);
        return myenterprise;
    }
    
    private Domain createdomain(String domainname,Enterprise enterprise) throws RestException {
    	Domain mydomain = new Domain();
    	mydomain.setName(domainname);
    	mydomain.setTemplateID("3b97f724-af51-11ed-9d7d-d5f1b8c771ee");
    	enterprise.createChild(mydomain);
    	return mydomain;
    }
    
    
    private Domain fetchDomainbyName(String domainname,Enterprise enterprise) throws RestException {
    	String filter = String.format("name == '%s'", domainname);
    	DomainsFetcher fetcher = enterprise.getDomains();
    	Domain	mydomain = fetcher.getFirst(filter, null, null, null, null, null, true);
        return mydomain;
    }
    
    private VPort createVPortInSubnet(String vportName, Subnet subnet) throws RestException {
        VPort vport = this.fetchVPortByNameForSubnet(vportName, subnet);
        if (vport == null) {
            vport = new VPort();
            vport.setName(vportName);
            subnet.createChild(vport);
            Date createDate = new Date(Long.parseLong(vport.getCreationDate()));
           // System.out.println("New VPort created with id " + vport.getId() + " at " + createDate.toString());
        } else {
            Date createDate = new Date(Long.parseLong(vport.getCreationDate()));
            System.out.println("Old VPort " + vport.getName() + " already created at " + createDate.toString());
        }
        return vport;
    }
    
    private VPort fetchVPortByNameForSubnet(String vportName, Subnet subnet) throws RestException {
        String filter = String.format("name == '%s'", vportName);
        VPortsFetcher fetcher = subnet.getVPorts();
        VPort vport = fetcher.getFirst(filter, null, null, null, null, null, true);
        return vport;
    }
    
    private Subnet fetchSubnetbydomainzone(String subnetname, Domain domain , String zonename) throws RestException {
    	 String filter = String.format("name == '%s'", zonename);
         ZonesFetcher fetcher = domain.getZones();
         Zone myzone = fetcher.getFirst(filter, null, null, null, null, null, true);
         filter = String.format("name == '%s'", subnetname);
         SubnetsFetcher subnetfetcher = myzone.getSubnets();
         Subnet mysubnet = subnetfetcher.getFirst(filter, null, null, null, null, null, true);
         return mysubnet;
    	
    }
    private VM createVMbyvport(String vmname, String vmuuid, String vport_id) throws RestException {
         VM myVM = new VM();
         List<VMInterface> myinterfaces = new ArrayList<>();
         VMInterface myinterface = new VMInterface();
         myinterface.setName("vnet0");
         myinterface.setMAC("52:54:00:cf:cb:e7");
         myinterface.setVPortID(vport_id);
         myinterfaces.add(myinterface);
         
         myVM.setName(vmname);
         myVM.setUUID(vmuuid);
         myVM.setInterfaces(myinterfaces);
         session.getMe().createChild(myVM);
         return myVM;
     	
    }
    
}