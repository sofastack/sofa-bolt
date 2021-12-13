package com.alipay.remoting;

import com.alipay.remoting.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author je
 * <p>
 * Rpc address, resolve the ip and protocol version of the Rpc address
 * The ip address should be followed by a':', for example "127.0.0.1:"
 *
 */
public class RpcAddress {

    /**
     * symbol :
     */
    private static final char COLON = ':';

    /**
     * Regular expression of standard IPv4 address: IPv4 address can be extracted from the string
     */
    private static final Pattern IPV4_REGEX_NORMAL = Pattern.compile(
            "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)");

    /**
     * Full format IPv6 regular expression: IPv6 address can be extracted from the string
     */
    private static final Pattern IPV6_REGEX_NORMAL = Pattern.compile(
            "\\[((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))](%.+)?");


    private final String url;
    private String ip;


    /**
     * Constructor, accepts url parameters, resolve the ip and protocol version of the Rpc address
     *
     * @param url url
     */
    private RpcAddress(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Illegal format address string [" + url
                    + "], should not be blank! ");
        }
        this.url = url;
        this.parse();

    }

    /**
     * create a rpcAddress object
     *
     * @param url url
     * @return rpcAddress
     */
    public static RpcAddress newRpcAddress(String url) {
        return new RpcAddress(url);
    }

    /**
     * resolve the ip and protocol version of the Rpc address
     */
    private void parse() {
        if (!tryParseV4() && !tryParseV6()) {
            throw new IllegalArgumentException("Illegal format address string [" + url
                    + "], It should conform to the ipv4 protocol or ipv6 protocol address format! ");
        }
    }

    /**
     * @return Whether it conforms to the IPv4 protocol format
     * @see #doParse(Pattern)
     */
    private boolean tryParseV4() {
        return doParse(IPV4_REGEX_NORMAL);
    }

    /**
     * @return Whether it conforms to the IPv6 protocol format
     * According to RFC2732, literal IPv6 addresses should be put inside square brackets in URLs, e.g. like this:
     * http://[1080:0:0:0:8:800:200C:417A]/index.html
     * @see #doParse(Pattern)
     */
    private boolean tryParseV6() {
        return doParse(IPV6_REGEX_NORMAL);
    }

    /**
     * resolve the ip and protocol version of the Rpc address
     *
     * @param pattern ipv4 or ipv6 regular expression
     * @return Whether it conforms to the regular expression format
     */
    private boolean doParse(Pattern pattern) {
        final Matcher matcher = pattern.matcher(url);
        String ip;
        if (matcher.find()) {
            ip = matcher.group();
        } else {
            return false;
        }
        if (url.startsWith(ip)) {
            final char firstCharAfterIp = url.charAt(ip.length());
            if (firstCharAfterIp != COLON) {
                throw new IllegalArgumentException("Illegal format address string [" + url
                        + "], must have one COLON[:] after ip string! ");
            }
            this.ip = ip;

            return true;
        }
        return false;
    }


    public String getIp() {
        return ip;
    }

}
