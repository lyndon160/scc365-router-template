package net.floodlightcontroller.practical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.IPv6;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.PacketParsingException;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.ICMPv4Type;
import org.projectfloodlight.openflow.types.ICMPv4Code;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.VlanVid;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U16;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFSetConfig;
import org.projectfloodlight.openflow.protocol.OFAsyncSet;
import org.projectfloodlight.openflow.protocol.OFConfigFlags;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetTpSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetTpDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetDlSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetDlDst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author your name
* SCC365 router coursework - Static routers with ICMP response and TTL capability
*
* This is a skeleton class
*/


public class Practical implements IFloodlightModule, IOFMessageListener {

  protected static Logger log = LoggerFactory.getLogger(Practical.class);
  protected IFloodlightProviderService floodlightProvider;
  protected HashMap<IOFSwitch, RouterData> routerData;


  @Override
  public String getName() {
    return "practical";
  }

  @Override
  public boolean isCallbackOrderingPrereq(OFType type, String name) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isCallbackOrderingPostreq(OFType type, String name) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
    Collection<Class<? extends IFloodlightService>> l =
    new ArrayList<Class<? extends IFloodlightService>>();
    l.add(IFloodlightProviderService.class);
    return l;
  }

  @Override
  public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void init(FloodlightModuleContext context) throws FloodlightModuleException {
    floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
    log = LoggerFactory.getLogger(Practical.class);
    routerData = new HashMap<IOFSwitch, RouterData>();
  }

  @Override
  public void startUp(FloodlightModuleContext context) {
    floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
  }

  @Override
  /* Handle a packet message - called every time a packet is received */
  public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {

    //OpenFlow factory - used for building OpenFlow messages
    OFFactory ofFactory = sw.getOFFactory();

    //Get Packet-In message
    OFPacketIn pi = (OFPacketIn) msg;
    OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));

    //Load Match
    Match match = createMatchFromPacket(sw, pi);
    log.debug("Match: {}", match.toString());

    //Get tables

    //Flood packet (this should be removed to complete the static router)
    writePacketToPort(sw, pi, OFPort.FLOOD.getPortNumber());

    //Allow Floodlight to continue processing the packet
    return Command.CONTINUE;
  }

  /**
  * Instead of using the Firewall's routing decision Match, which might be as general
  * as "in_port" and inadvertently Match packets erroneously, construct a more
  * specific Match based on the deserialized OFPacketIn's payload, which has been
  * placed in the FloodlightContext already by the Controller.
  *
  * @param sw, the switch on which the packet was received
  * @param pi, PacketIn message
  * @return a composed Match object based on the provided information
  */
  protected Match createMatchFromPacket(IOFSwitch sw, OFPacketIn pi) {

    Ethernet eth = new Ethernet();
    eth.deserialize(pi.getData(), 0, pi.getTotalLen());
    VlanVid vlan = VlanVid.ofVlan(eth.getVlanID());
    MacAddress srcMac = eth.getSourceMACAddress();
    MacAddress dstMac = eth.getDestinationMACAddress();

    Match.Builder mb = sw.getOFFactory().buildMatch();
    mb.setExact(MatchField.IN_PORT, (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT)));

    //mac
    mb.setExact(MatchField.ETH_SRC, srcMac).setExact(MatchField.ETH_DST, dstMac);

    //vlan
    if (!vlan.equals(VlanVid.ZERO)) {
      mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
    }

    // TODO Detect switch type and match to create hardware-implemented flow
    if (eth.getEtherType() == EthType.IPv4) { /* shallow check for equality is okay for EthType */
      IPv4 ip = (IPv4) eth.getPayload();
      IPv4Address srcIp = ip.getSourceAddress();
      IPv4Address dstIp = ip.getDestinationAddress();

      //ipv4
      mb.setExact(MatchField.ETH_TYPE, EthType.IPv4)
      .setExact(MatchField.IPV4_SRC, srcIp)
      .setExact(MatchField.IPV4_DST, dstIp);

      /*
      * Take care of the ethertype if not included earlier,
      * since it's a prerequisite for transport ports.
      */
      mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);

      //transport
      if (ip.getProtocol().equals(IpProtocol.TCP)) {
        TCP tcp = (TCP) ip.getPayload();
        mb.setExact(MatchField.IP_PROTO, IpProtocol.TCP)
        .setExact(MatchField.TCP_SRC, tcp.getSourcePort())
        .setExact(MatchField.TCP_DST, tcp.getDestinationPort());
      } else if (ip.getProtocol().equals(IpProtocol.UDP)) {
        UDP udp = (UDP) ip.getPayload();
        mb.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
        .setExact(MatchField.UDP_SRC, udp.getSourcePort())
        .setExact(MatchField.UDP_DST, udp.getDestinationPort());
      } else if (ip.getProtocol().equals(IpProtocol.ICMP)){
        ICMP icmp = (ICMP) ip.getPayload();
        mb.setExact(MatchField.IP_PROTO, IpProtocol.ICMP)
        .setExact(MatchField.ICMPV4_TYPE, ICMPv4Type.of(icmp.getIcmpType()))
        .setExact(MatchField.ICMPV4_CODE, ICMPv4Code.of(icmp.getIcmpCode()));
      }

    } else if (eth.getEtherType() == EthType.ARP) { /* shallow check for equality is okay for EthType */
      mb.setExact(MatchField.ETH_TYPE, EthType.ARP);
    } else if (eth.getEtherType() == EthType.IPv6) {
      IPv6 ip = (IPv6) eth.getPayload();
      IPv6Address srcIp = ip.getSourceAddress();
      IPv6Address dstIp = ip.getDestinationAddress();

      //ipv6
      mb.setExact(MatchField.ETH_TYPE, EthType.IPv6)
      .setExact(MatchField.IPV6_SRC, srcIp)
      .setExact(MatchField.IPV6_DST, dstIp);

      /*
      * Take care of the ethertype if not included earlier,
      * since it's a prerequisite for transport ports.
      */
      mb.setExact(MatchField.ETH_TYPE, EthType.IPv6);

      //transport
      if (ip.getNextHeader().equals(IpProtocol.TCP)) {
        TCP tcp = (TCP) ip.getPayload();
        mb.setExact(MatchField.IP_PROTO, IpProtocol.TCP)
        .setExact(MatchField.TCP_SRC, tcp.getSourcePort())
        .setExact(MatchField.TCP_DST, tcp.getDestinationPort());
      } else if (ip.getNextHeader().equals(IpProtocol.UDP)) {
        UDP udp = (UDP) ip.getPayload();
        mb.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
        .setExact(MatchField.UDP_SRC, udp.getSourcePort())
        .setExact(MatchField.UDP_DST, udp.getDestinationPort());
      }
    }
    return mb.build();
  }

  /*
  * Write a packet out to a specific port
  */
  public void writePacketToPort(IOFSwitch sw, OFPacketIn pi, ArrayList<OFAction> actions){

    OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();
    pob.setBufferId(pi.getBufferId());
    pob.setInPort((pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT)));
    pob.setXid(pi.getXid());
    pob.setActions(actions);

    if (pi.getBufferId() == OFBufferId.NO_BUFFER) {
      pob.setData(pi.getData());
    }

    sw.write(pob.build());
  }


  /*
  * Write an ICMP reply packet out that is replying to the packet in ICMP request
  *
  * @param sw
  * @param pi
  * @param dst_ipaddr The destination IP address (as String) the ICMP reply is going to (found from the source IP address of the ICMP request)
  * @param dst_dladdr The destination MAC address (as byte array) this is found from the source MAC address of the IMCP request
  * @param src_ipaddr The source IP address (as String) Note this should be the same IP address associated with the outPort
  * @param src_dladdr The source MAC address (as byte array) Note this should be the same MAC address associate with the outPort
  * @param outPort The port on the OpenFlow switch that the packet will be send out on
  */
  public void writeICMPToPort (IOFSwitch sw, OFPacketIn pi, IPv4Address dstIP, MacAddress dstMAC, IPv4Address srcIP, MacAddress srcMAC, int outPort,  byte icmpType, byte icmpCode) {
    OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();

    Ethernet l2 = new Ethernet();
    l2.setSourceMACAddress(srcMAC);
    l2.setDestinationMACAddress(dstMAC);
    l2.setEtherType(EthType.IPv4);

    IPv4 l3 = new IPv4();
    l3.setSourceAddress(srcIP);
    l3.setDestinationAddress(dstIP);
    l3.setProtocol(IpProtocol.ICMP);
    l3.setTtl((byte) 64);

    ICMP icmp = new ICMP();
    try{
      switch(icmpType){
        case 0:
        icmp.deserialize(pi.getData(), 34, pi.getTotalLen() - 34);
        break;
        case 3:{
          IPv4 ip = new IPv4();
          ip.deserialize(pi.getData(), 14, pi.getTotalLen() - 14);
          icmp.setPayload(ip);
          break;
        }case 11:{
          IPv4 ip = new IPv4();
          ip.deserialize(pi.getData(), 14, pi.getTotalLen() - 14);
          icmp.setPayload(ip);
          break;
        }default:
        break;
      }
    }catch(PacketParsingException e){
      log.debug("Could not deserialize packet data to ICMP");
      return;
    }

    icmp.setIcmpType(icmpType);
    icmp.setIcmpCode(icmpCode);

    l2.setPayload(l3);
    l3.setPayload(icmp);

    OFActionOutput.Builder outputBuilder = sw.getOFFactory().actions().buildOutput().setPort(OFPort.of(outPort));
    pob.setActions(Collections.singletonList((OFAction)outputBuilder.build()));
    pob.setBufferId(OFBufferId.NO_BUFFER);
    pob.setData(l2.serialize());

    sw.write(pob.build());
  }


  /* Install a flow-mod with given parameters */
  private void installFlowMod(IOFSwitch sw, OFPacketIn pi, Match match, ArrayList<OFAction> actions, int idleTimeout, int hardTimeout, int priority){
    OFFlowAdd.Builder fmb = sw.getOFFactory().buildFlowAdd();
    fmb.setBufferId(OFBufferId.of(-1));
    fmb.setMatch(match);
    fmb.setIdleTimeout(idleTimeout);
    fmb.setHardTimeout(hardTimeout);
    fmb.setPriority(priority);
    fmb.setActions(actions);
    sw.write(fmb.build());
  }

  /* Gets the IP TTL from the PacketIn data - should probably check that its IP first */
  private byte getTTL(OFPacketIn pi){
    Ethernet eth    = new Ethernet();
    eth.deserialize(pi.getData(), 0, pi.getTotalLen());
    IPv4 ip         = (IPv4) eth.getPayload();
    return ip.getTtl();
  }

  @Override
  public Collection<Class<? extends IFloodlightService>> getModuleServices() {
    // We don't provide any services, return null
    return null;
  }


}
