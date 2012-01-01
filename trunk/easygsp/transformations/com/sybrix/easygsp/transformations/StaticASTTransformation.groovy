package com.sybrix.easygsp.transformations;


import java.lang.reflect.Modifier
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * EasyGSPTransformation <br/>
 *
 * @author David Lee
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class StaticASTTransformation implements ASTTransformation {

        private static int i = 0;

        public void visit(ASTNode[] astNodes, SourceUnit source) {
                if (!astNodes) return

                //def annotation = new AnnotationNode(ClassHelper.make(Mixin, false))
                //annotation.setMember('value', new ClassExpression(new ClassNode(StaticMethods.class)))

                def x = ClassHelper.make(com.sybrix.easygsp.http.StaticMethods.class, false)
                def x2 = ClassHelper.make(com.sybrix.easygsp.util.Framework.class, false)
                //def x3 = ClassHelper.make(easyom.EasyOM.class, false)


                (astNodes[0] as ModuleNode).addStaticStarImport('utilMethods', x)
                (astNodes[0] as ModuleNode).addStaticStarImport('framework', x2)
                //(astNodes[0] as ModuleNode).addStaticImport(x3,'withTransaction','wt')

                //(astNodes[0] as ModuleNode).addStaticStarImport('easyom', x3)
                (astNodes[0] as ModuleNode).addStarImport('models.')
                (astNodes[0] as ModuleNode).addStarImport('util.')
                (astNodes[0] as ModuleNode).addStarImport('services.')
                //(astNodes[0] as ModuleNode).addStarImport('easyom.')

        }


}                                    




