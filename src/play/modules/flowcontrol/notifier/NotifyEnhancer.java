/**
 * NotifyEnhancer 11.02.2013
 *
 * @author Philipp Haussleiter
 *
 */
package play.modules.flowcontrol.notifier;

import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.EnumMemberValue;
import play.Logger;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

public class NotifyEnhancer extends Enhancer {
    private static final String PLAY_MVC_WITH = "play.mvc.With";

    @Override
    public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {
        CtClass ctClass = makeClass(applicationClass);
        Logger.info("check enhancing %s", ctClass.getName());
        if (!ctClass.getName().equals("ActionInvoker")) {
            return;
        }
        ConstPool constpool = ctClass.getClassFile().getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        if (!hasAnnotation(ctClass, PLAY_MVC_WITH)) {
            Annotation annot = new Annotation(PLAY_MVC_WITH, constpool);
            EnumMemberValue enumValue = new EnumMemberValue(constpool);
            enumValue.setType(PLAY_MVC_WITH);
            enumValue.setValue("Notifier.class");
            annot.addMemberValue("value", enumValue);
            attr.addAnnotation(annot);
            ctClass.getClassFile().addAttribute(attr);

        }
        applicationClass.enhancedByteCode = ctClass.toBytecode();
        ctClass.defrost();
    }
}
