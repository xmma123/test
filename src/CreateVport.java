

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
public class CreateVport implements Runnable {
    private static final String MY_VSD_SERVER_PORT = "https://192.168.56.106:8443";
    private static final VSDSession session;
    private int Startidx;
    private int Range;

    static {
        session = new VSDSession("csproot", "csproot", "csp", MY_VSD_SERVER_PORT);
        session.getClientTemplate().disableCertificateValidation();
    }
    public CreateVport(int indx,int ranges){
    	this.Startidx=indx;
    	this.Range=ranges;
    }
    @Override
    public void run() {
        System.out.println("Fetching All Enterprises");
        try {
        session.start();
//        deleteallVM();
        Enterprise myenterprise = fetchEnterprisebyName("mypc");
 //       System.out.println("Number of Enterprises found : " + enterprises.size());
        for (int vpccounter = Startidx; vpccounter < Startidx + Range ; vpccounter ++) {
        String vpcname = "VPC-" + String.valueOf(vpccounter);        
        Domain mydomain = fetchDomainbyName(vpcname,myenterprise);
        if ( mydomain == null) {
        	System.out.println("Not Found Enterprise: " + myenterprise.getName() + " domain: " + vpcname);
        	continue;
        }

        Subnet mysubnet = fetchSubnetbydomainzone("subnet1",mydomain,"zone0");
        
         System.out.println("Found Enterprise: " + myenterprise.getName() + " domain: " + mydomain.getName() + " subnet: " + mysubnet.getName());
         VPort myvport=createVPortInSubnet("vport1",mysubnet);
         
         VM  myvm = createVMbyvport("vm1", UUID.randomUUID().toString(),myvport.getId(),"10.40.18.1"); 
//         System.out.println("Create Vport: " + myvport.getName() + " VM : " + myvm.getName());
        } 
        } catch (Exception e){
        	throw new RuntimeException("Thread end!!!" + e.toString());
        }    
    }
    
    public static void main(String[] args) {
    	int allcount=10000;
    	int singlecount=2000;
    	int threads=allcount/singlecount;
    	
        for (int threadid = 0; threadid < threads; threadid ++) 
        {
    	   new Thread(new CreateVport((threadid * singlecount + 1),singlecount)).start();
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
    private VM createVMbyvport(String vmname, String vmuuid, String vport_id,String address) throws RestException {
         VM myVM = new VM();
         List<VMInterface> myinterfaces = new ArrayList<>();
         VMInterface myinterface = new VMInterface();
         myinterface.setName("vnet0");
         myinterface.setMAC("52:54:00:cf:cb:e7");
         myinterface.setVPortID(vport_id);
         myinterface.setIPAddress(address);
         myinterfaces.add(myinterface);
         
         myVM.setName(vmname);
         myVM.setUUID(vmuuid);
         myVM.setInterfaces(myinterfaces);
         session.getMe().createChild(myVM);
         return myVM;
     	
    }
    
    private void deleteallVM() throws RestException {
    	VMsFetcher fetcher = session.getMe().getVMs();
    	List<VM> vms = fetcher.get();
    	for (VM vm : vms) {
    		System.out.println("delete VM:" + vm.getName());
    		vm.delete();
    	}
    }
    
}