/**
 * FlowcontrolNotifierPlugin
 * 05.08.2012
 * @author Philipp Haussleiter
 *
 */
package play.modules.flowcontrol.notifier;

import play.Logger;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;

public class FlowcontrolNotifierPlugin extends PlayPlugin {

    private NotifyEnhancer enhancer = new NotifyEnhancer();
    @Override
    public void onApplicationStart() {
        Logger.info("FlowcontrolNotifierPlugin active!");
    }
    
    @Override
    public void enhance(ApplicationClass applicationClass) throws Exception {
        enhancer.enhanceThisClass(applicationClass);
    }
}
