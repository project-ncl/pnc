package org.jboss.pnc.common.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public final class UrlUtils
{

    private UrlUtils()
    {
    }

    public static String buildUrl( final String baseUrl, final String... parts )
        throws MalformedURLException
    {
        return buildUrl( baseUrl, null, parts );
    }

    public static String buildUrl( final String baseUrl, final Map<String, String> params, final String... parts )
        throws MalformedURLException
    {
        if ( parts == null || parts.length < 1 )
        {
            return baseUrl;
        }

        final StringBuilder urlBuilder = new StringBuilder();

        if ( parts[0] == null || !parts[0].startsWith( baseUrl ) )
        {
            urlBuilder.append( baseUrl );
        }

        for ( String part : parts )
        {
            if ( part == null || part.trim()
                                     .length() < 1 )
            {
                continue;
            }

            if ( part.startsWith( "/" ) )
            {
                part = part.substring( 1 );
            }

            if ( urlBuilder.length() > 0 && urlBuilder.charAt( urlBuilder.length() - 1 ) != '/' )
            {
                urlBuilder.append( "/" );
            }

            urlBuilder.append( part );
        }

        if ( params != null && !params.isEmpty() )
        {
            urlBuilder.append( "?" );
            boolean first = true;
            for ( final Map.Entry<String, String> param : params.entrySet() )
            {
                if ( first )
                {
                    first = false;
                }
                else
                {
                    urlBuilder.append( "&" );
                }

                urlBuilder.append( param.getKey() )
                          .append( "=" )
                          .append( param.getValue() );
            }
        }

        return new URL( urlBuilder.toString() ).toExternalForm();
    }

}
