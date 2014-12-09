package org.jboss.pnc.common.test.util;

import org.jboss.pnc.common.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
public class StringUtilsTest {

    @Test
    public void replaceEnvironmentVariableTestCase() {
        String src = "JAVA_HOME:${env.JAVA_HOME}";
        String replaced = StringUtils.replaceEnv(src);
        Assert.assertEquals("JAVA_HOME:" + System.getenv("JAVA_HOME"), replaced);

    }
}
