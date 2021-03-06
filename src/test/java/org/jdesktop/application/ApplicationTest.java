/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package org.jdesktop.application;

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * ApplicationTest.java
 * <p/>
 * This test depends on ResourceBundles and an image file:
 * <pre>
 * resources/
 * resources/black1x1.png
 * </pre>
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ApplicationTest extends TestCase {
    private static boolean isAppLaunched = false;

    public static class SimpleApplication extends WaitForStartupApplication {
        public boolean startupOnEDT;

        @Override
        protected void startup() {
            super.startup();
            startupOnEDT = SwingUtilities.isEventDispatchThread();
        }

        @ProxyAction()
        public void simpleAppAction() {
        }
    }

    public ApplicationTest(String testName) {
        super(testName);
        if (!isAppLaunched) {
            SimpleApplication.launchAndWait(SimpleApplication.class);
            isAppLaunched = true;
        }
    }

    private ApplicationContext getApplicationContext() {
        return Application.getInstance(SimpleApplication.class).getContext();
    }

    public void testLaunch() {
        ApplicationContext ac = getApplicationContext();
        Application app = ac.getApplication();
        boolean isSimpleApp = app instanceof SimpleApplication;
        assertTrue("ApplicationContext.getApplication()", isSimpleApp);
        Class appClass = ac.getApplicationClass();
        assertSame("ApplicationContext.getApplicationClass()", SimpleApplication.class, appClass);
        assertTrue("SimpleApplication.startup() ran on the EDT", ((SimpleApplication) app).startupOnEDT);
    }

    public void testGetResourceMap() {
        ApplicationContext ac = getApplicationContext();
        String bundleBaseName = getClass().getPackage().getName() + ".resources.";

        /* Check the Application ResourceMap chain */
        {
            ResourceMap appRM = ac.getResourceMap();
            assertNotNull("Application ResourceMap", appRM);
            /* Application ResourceMap rm should have a null parent
              * and three bundles:
              */
            String[] expectedBundleNames = {
                    bundleBaseName + "SimpleApplication",
                    bundleBaseName + "WaitForStartupApplication",
                    bundleBaseName + "Application"
            };
            assertEquals(expectedBundleNames.length, appRM.getBundleNames().size());
            int i = 0;
            for (String expectedBundleName : expectedBundleNames) {
                assertEquals(expectedBundleName, appRM.getBundleNames().get(i));
                i += 1;
            }
        }
        /* Check the ResourceMap for getClass() */
        {
            ResourceMap rm = ac.getResourceMap(getClass());
            assertNotNull(rm);
            assertEquals(bundleBaseName + "ApplicationTest", rm.getBundleNames().get(0));
        }
    }

    /**
     * Verify that the platform resource was initialized to "osx" or "default"
     * and that it can be reset.
     */
    public void testPlatformResource() {
        ApplicationContext ctx = getApplicationContext();
        ResourceMap appRM = ctx.getResourceMap();
        String platform = appRM.getString("platform");
        assertTrue("default".equals(platform) || "osx".equals(platform));
        ctx.getResourceManager().setPlatform("anotherPlatform");
        assertEquals("anotherPlatform", ctx.getResourceManager().getPlatform());
        assertEquals("anotherPlatform", appRM.getString("platform"));
    }

    private void checkActionName(String msg, javax.swing.Action action, String expectedValue) {
        String value = (String) (action.getValue(javax.swing.Action.NAME));
        assertEquals(msg + ".getValue(javax.swing.Action.NAME)", expectedValue, value);
    }

    public static class SimpleController {
        @ProxyAction()
        public void simpleControllerAction() {
        }
    }

    /**
     * Verify that the ActionMap for SimplController.class contains an Action
     * for "simpleControllerAction" and a parent ActionMap, defined by
     * SimpleControllerApp, that contains "simpleControllerAppAction".
     */
    public void testGetActionMap() {
        SimpleController sc = new SimpleController();
        /* There should be four ActionMaps in the parent chain for sc, based on:
          * 0 - SimpleController.class
          * 1 - SimpleApplication.class
          * 1 - WaitForStartupApplication.class
          * 2 - Application.class  // parent of this one should be null
          */
        Class[] actionsClasses = {
                SimpleController.class,
                SimpleApplication.class,
                WaitForStartupApplication.class,
                Application.class
        };
        ApplicationActionMap actionMap = getApplicationContext().getActionMap(sc);
        int n = 0;
        for (Class actionsClass : actionsClasses) {
            assertNotNull("ActionMap " + actionsClass + " " + n, actionMap);
            assertSame("ActionMap actionsClass " + n, actionsClass, actionMap.getActionsClass());
            actionMap = (ApplicationActionMap) actionMap.getParent();
        }
        assertNull("Application actionMap parent", actionMap);

        actionMap = getApplicationContext().getActionMap(sc);
        String simpleControllerAction = "simpleControllerAction";
        String gamString = "Application.getActionMap(simpleController)";
        String gscaString = gamString + ".get(\"" + simpleControllerAction + "\")";
        javax.swing.Action scAction = actionMap.get(simpleControllerAction);
        assertNotNull(gscaString, scAction);
        checkActionName(gscaString, scAction, simpleControllerAction);

        String simpleAppAction = "simpleAppAction";
        String gsaaString = gamString + ".get(\"" + simpleAppAction + "\")";
        javax.swing.Action saAction = actionMap.get(simpleAppAction);
        assertNotNull(gsaaString, saAction);
        checkActionName(gsaaString, saAction, simpleAppAction);

        String noSuchAction = "noSuchAction";
        String gnsaString = gamString + ".get(\"" + noSuchAction + "\")";
        javax.swing.Action nsAction = actionMap.get(noSuchAction);
        assertNull(gnsaString, nsAction);
    }

    /**
     * Check the ActionMap returned by ApplicationContext.getActionMap(),
     * i.e. the global ApplicationMap.
     */
    public void testGetAppActionMap() {
        /* In this case there should be just three ActionMaps in the
          * parent chain:
          * 0 - SimpleApplication.class
          * 1 - WaitForStartupApplication.class
          * 2 - Application.class  // parent of this one should be null
          */
        Class[] actionsClasses = {
                SimpleApplication.class,
                WaitForStartupApplication.class,
                Application.class
        };
        ApplicationActionMap actionMap = getApplicationContext().getActionMap();
        int n = 0;
        for (Class actionsClass : actionsClasses) {
            assertNotNull("ActionMap " + actionsClass + " " + n, actionMap);
            assertSame("ActionMap actionsClass " + n, actionsClass, actionMap.getActionsClass());
            actionMap = (ApplicationActionMap) actionMap.getParent();
        }
        assertNull("Application actionMap parent", actionMap);

        actionMap = getApplicationContext().getActionMap();
        String simpleControllerAction = "simpleControllerAction";
        String gamString = "Application.getActionMap()";
        String gscaString = gamString + ".get(\"" + simpleControllerAction + "\")";
        javax.swing.Action scAction = actionMap.get(simpleControllerAction);
        assertNull(gscaString, scAction);

        String simpleAppAction = "simpleAppAction";
        String gsaaString = gamString + ".get(\"" + simpleAppAction + "\")";
        javax.swing.Action saAction = actionMap.get(simpleAppAction);
        assertNotNull(gsaaString, saAction);
        checkActionName(gsaaString, saAction, simpleAppAction);
    }

    public static class StatefulController {
        private int n = 0;

        @ProxyAction()
        public void one() {
            n = 1;
        }

        @ProxyAction()
        public void two() {
            n = 2;
        }

        public int getN() {
            return n;
        }
    }

    private void checkSCActionMap(ApplicationActionMap appAM, Object actionsObject, Class actionsClass) {
        String msg = "ActionMap for " + actionsClass;
        assertNotNull(msg, appAM);
        assertSame(msg + ".getActionsObject()", actionsObject, appAM.getActionsObject());
        assertSame(msg + ".getActionsClass()", actionsClass, appAM.getActionsClass());
        assertNotNull(msg + ".getAction(\"one\")", appAM.get("one"));
        assertNotNull(msg + ".getAction(\"two\")", appAM.get("two"));
    }

    /**
     * Verify that getActionMap() caches per target actionsObject, not per @Actions
     * class.
     */
    public void testGetActionMapPerObject() {
        StatefulController sc1 = new StatefulController();
        StatefulController sc2 = new StatefulController();
        StatefulController sc3 = new StatefulController();
        ApplicationContext ac = getApplicationContext();
        ApplicationActionMap am1 = ac.getActionMap(sc1);
        ApplicationActionMap am2 = ac.getActionMap(sc2);
        ApplicationActionMap am3 = ac.getActionMap(sc3);
        checkSCActionMap(am1, sc1, StatefulController.class);
        checkSCActionMap(am2, sc2, StatefulController.class);
        checkSCActionMap(am3, sc3, StatefulController.class);
        String oneActionMapPerObject = "one ActionMap per actionsObject";
        assertTrue(oneActionMapPerObject, am1 != am2);
        assertTrue(oneActionMapPerObject, am1 != am3);
        assertTrue(oneActionMapPerObject, am2 != am3);
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "not used");
        am1.get("one").actionPerformed(event);
        am2.get("two").actionPerformed(event);
        assertEquals("StatefulController.getN(), after calling @Action sc1.one()", 1, sc1.getN());
        assertEquals("StatefulController.getN(), after calling @Action sc2.two()", 2, sc2.getN());
        assertEquals("StatefulController.getN(), no @Actions called", 0, sc3.getN());
    }


    public static class ActionMapObject {
        int n;

        ActionMapObject(int n) {
            this.n = n;
        }

        @ProxyAction()
        public void anAction() {
            n = -1;
        }
    }

    /**
     * Verify that ActionMaps are GC'd when their target actionsObject is
     * no longer referred to and their actions are no longer in use (no
     * longer referred to).
     */
    public void testActionMapGC() {
        ApplicationContext ac = getApplicationContext();
        List<Reference<ActionMapObject>> refs = new ArrayList<Reference<ActionMapObject>>();
        for (int i = 0; i < 256; i++) {
            ActionMapObject amo = new ActionMapObject(i);
            refs.add(new WeakReference<ActionMapObject>(amo));
            ApplicationActionMap appAM = ac.getActionMap(amo);
            assertNotNull(appAM);
            assertNotNull(appAM.get("anAction"));
            assertSame(amo, appAM.getActionsObject());
            assertEquals(i, amo.n);
        }
        /* GC should clear all of the references to ActionMapObjects because
          * they're no longer strongly reachable, i.e. the framework isn't
          * hanging on to them.
          */
        System.gc();
        for (Reference ref : refs) {
            assertNull("Reference to ApplictionActionMap actionsObject", ref.get());
        }
    }

    /**
     * Verify that an Action's target is -not- GC'd if a reference to the
     * Action persists even after no direct references to the
     * actionsObject target exist.
     */
    public void testActionMapNoGC() {
        ApplicationContext ac = getApplicationContext();
        List<Reference<ActionMapObject>> refs = new ArrayList<Reference<ActionMapObject>>();
        List<ApplicationAction> actions = new ArrayList<ApplicationAction>();
        for (int i = 0; i < 256; i++) {
            ActionMapObject amo = new ActionMapObject(i);
            refs.add(new WeakReference<ActionMapObject>(amo));
            ApplicationActionMap appAM = ac.getActionMap(amo);
            assertNotNull(appAM);
            actions.add((ApplicationAction) (appAM.get("anAction")));
            assertSame(amo, appAM.getActionsObject());
            assertEquals(i, amo.n);
        }
        /* GC should -not- clear all of the references to ActionMapObjects because
          * the ApplicationAction objects still refer (indirectly and strongy) 
          * to them. 
          */
        System.gc();
        for (Reference ref : refs) {
            assertNotNull("Reference to ApplictionActionMap actionsObject", ref.get());
        }
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "not used");
        int i = 0;
        for (ApplicationAction action : actions) {
            action.actionPerformed(event);
            ActionMapObject amo = refs.get(i).get();
            assertEquals("after calling ActionMapObject.anAction()", -1, amo.n);
        }
    }
}
