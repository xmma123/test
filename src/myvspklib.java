import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.nuagenetworks.bambou.RestException;
import net.nuagenetworks.vspk.v6.*;
import net.nuagenetworks.vspk.v6.IngressAdvFwdEntryTemplate.EAction;
import net.nuagenetworks.vspk.v6.IngressAdvFwdEntryTemplate.ELocationType;
import net.nuagenetworks.vspk.v6.IngressAdvFwdEntryTemplate.ENetworkType;
import net.nuagenetworks.vspk.v6.IngressAdvFwdTemplate.EPolicyState;
import net.nuagenetworks.vspk.v6.RedirectionTarget.EEndPointType;
import net.nuagenetworks.vspk.v6.StaticRoute.EIPType;
import net.nuagenetworks.vspk.v6.StaticRoute.EType;
import net.nuagenetworks.vspk.v6.fetchers.*;


public class myvspklib implements Runnable {
    public static final String MY_VSD_SERVER_PORT = "https://192.168.56.106:8443";
    public static  VSDSession session;

    public void run() {
    	   try {
		   session = new VSDSession("csproot", "csproot", "csp", MY_VSD_SERVER_PORT);
	       session.getClientTemplate().disableCertificateValidation();
		   session.start();   
    	   } catch (Exception e){
	        	throw new RuntimeException("Thread end!!!" + e.toString());
	        }
    }
    

	   
	   public myvspklib()  throws RestException {

	   }

	   public List<Enterprise> fetchAllEnterprises() throws RestException {
	        EnterprisesFetcher fetcher = session.getMe().getEnterprises();
	        List<Enterprise> enterprises = fetcher.get();
	        return enterprises;
	    }
	    
	    public Enterprise fetchEnterprisebyName(String enterprisename) throws RestException {
	    	String filter = String.format("name == '%s'", enterprisename);
	    	EnterprisesFetcher fetcher = session.getMe().getEnterprises();
	    	Enterprise	myenterprise = fetcher.getFirst(filter, null, null, null, null, null, true);
	        return myenterprise;
	    }
	    
	    public Domain fetchDomainbyName(String domainname,Enterprise enterprise) throws RestException {
	    	String filter = String.format("name == '%s'", domainname);
	    	DomainsFetcher fetcher = enterprise.getDomains();
	    	Domain	mydomain = fetcher.getFirst(filter, null, null, null, null, null, true);
	        return mydomain;
	    }
	    
	    public VPort createVPortInSubnet(String vportName, Subnet subnet) throws RestException {
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
	    
	    public VPort fetchVPortByNameForSubnet(String vportName, Subnet subnet) throws RestException {
	        String filter = String.format("name == '%s'", vportName);
	        VPortsFetcher fetcher = subnet.getVPorts();
	        VPort vport = fetcher.getFirst(filter, null, null, null, null, null, true);
	        return vport;
	    }
	    
	    public Subnet fetchSubnetbydomainzone(String subnetname, Domain domain , String zonename) throws RestException {
	    	 String filter = String.format("name == '%s'", zonename);
	         ZonesFetcher fetcher = domain.getZones();
	         Zone myzone = fetcher.getFirst(filter, null, null, null, null, null, true);
	         filter = String.format("name == '%s'", subnetname);
	         SubnetsFetcher subnetfetcher = myzone.getSubnets();
	         Subnet mysubnet = subnetfetcher.getFirst(filter, null, null, null, null, null, true);
	         return mysubnet;
	    	
	    }
	    public VM createVMbyvport(String vmname, String vmuuid, String vport_id,String address) throws RestException {
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
	    
	    public void deleteallVM() throws RestException {
	    	VMsFetcher fetcher = session.getMe().getVMs();
	    	List<VM> vms = fetcher.get();
	    	for (VM vm : vms) {
	    		System.out.println("delete VM:" + vm.getName());
	    		vm.delete();
	    	}
	    }
	    
	    public VirtualIP createVIPforVPort(String vip,VPort myvport) throws RestException  {
	    	 VirtualIP myvip = new  VirtualIP();
	    	 myvip.setVirtualIP(vip);
	    	 myvport.createChild(myvip);
	    	 return myvip;
	    }
	    public void createstaticroutebydomain(String prefix,String netmask,
	    		     String nexthop,Domain mydomain) throws RestException  {
	    	StaticRoute myroute= new StaticRoute();
	    	myroute.setType(EType.OVERLAY);
	    	myroute.setIPType(EIPType.IPV4);
	    	myroute.setAddress(prefix);
	    	myroute.setNetmask(netmask);
	    	myroute.setNextHopIp(nexthop);
	    	mydomain.createChild(myroute, 1, true);
	    	
	    }
	    public RedirectionTarget createredirectiontarget(String RTname,Domain mydomain,
	    		                   String zonename,String subnetname, String portname) throws RestException  {
	    	RedirectionTarget myRT = new RedirectionTarget();
	    	myRT.setName(RTname);
	    	myRT.setEndPointType(EEndPointType.L3);
	    	VPort myvport=fetchVPortByNameForSubnet(portname,fetchSubnetbydomainzone(subnetname,mydomain,zonename));
	    	mydomain.createChild(myRT);
	    	myRT.assignOne(myvport);
	    	
	    	return myRT;
	    }
	    public IngressAdvFwdTemplate createingressfwtemplate(String templatename,Domain mydomain) throws RestException  {
	    	IngressAdvFwdTemplate myingressafwtemplate = new IngressAdvFwdTemplate();
	    	myingressafwtemplate.setName(templatename);
	    	myingressafwtemplate.setActive(true);
	    	mydomain.createChild(myingressafwtemplate);
	    	return myingressafwtemplate;
	    }
	    public void createingressfwentrytemplate(IngressAdvFwdTemplate myingressafwtemplate,String destport,
	    		                                     RedirectionTarget myRT) throws RestException  {
	    	IngressAdvFwdEntryTemplate myentry = new IngressAdvFwdEntryTemplate();
	    	myentry.setACLTemplateName("rule-" + destport);
	    	myentry.setAction(EAction.REDIRECT);
	    	myentry.setDSCP("*");
	    	myentry.setEtherType("0x0800");
	    	myentry.setLocationType(ELocationType.ANY);
	    	myentry.setProtocol("6");
	    	myentry.setDestinationPort(destport);
	    	myentry.setSourcePort("*");
	    	myentry.setNetworkType(ENetworkType.ANY);
	    	myentry.setRedirectVPortTagID(myRT.getId());
	    	myingressafwtemplate.createChild(myentry, 1,true);
	    	
	    }
	}
	
