/** Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */

package omc.corba;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OmcAPITest {
  private OMCInterface omc;
  @Before
  public void initClient() throws IOException {
    omc = new OMCClient("/usr/local/bin/omc", "en_US.UTF-8");
    omc.connect();
  }
  @After
  public void stopClient() throws IOException {
    omc.disconnect();
  }

  @Test
  public void is_test() throws ConnectException, IOException {
    omc.sendExpression("class test end test;");
    assertTrue(omc.is_("Class", "test"));
    assertFalse(omc.is_("Class", "test2"));
    omc.sendExpression("model nico end nico;");
    assertTrue(omc.is_("Model", "nico"));
  }

  @Test
  public void getListTest() throws ConnectException, IOException {
    omc.call("clear");
    omc.sendExpression("model test end test;");
    assertEquals(Arrays.asList("test"), omc.getList("getClassNames"));
    assertEquals(Collections.emptyList(), omc.getList("getPackages"));
  }

  @Test
  public void checkModelTest() throws ConnectException, IOException {
    omc.call("clear");
    omc.sendExpression("model test Intger x; equation x = 0.5; end test;");
    String exp = "[<interactive>:1:12-1:19:writable] Error: Class Intger not found in scope test.\nError: Error occurred while flattening model test";
    assertEquals(exp, omc.checkModel("test"));
    omc.sendExpression("model test2 Integer x; equation x = 1; end test2;");
    String exp2 = "\"Check of test2 completed successfully.\nClass test2 has 1 equation(s) and 1 variable(s).\n1 of these are trivial equation(s).\"";
    assertEquals(exp2, omc.checkModel("test2"));
  }

  @Test
  public void getClassInformationTest() throws ConnectException, IOException {
    Optional<String> info = omc.getClassInformation("Modelica");
    assertTrue(info.isPresent());
    assertTrue(info.get().contains("package"));

    Optional<String> info2 = omc.getClassInformation("Modelica.Electrical");
    assertTrue(info2.isPresent());
    assertTrue(info2.get().contains("Library of electrical models (analog, digital, machines, multi-phase)"));
  }
}
