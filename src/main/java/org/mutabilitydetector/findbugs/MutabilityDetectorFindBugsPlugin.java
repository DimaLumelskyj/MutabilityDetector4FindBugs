/*
 * FindBugs4JUnit. Copyright (c) 2011 youDevise, Ltd.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/

package org.mutabilitydetector.findbugs;

import static org.mutabilitydetector.locations.Dotted.dotted;

import java.util.Map;

import org.mutabilitydetector.AnalysisResult;
import org.mutabilitydetector.IsImmutable;
import org.mutabilitydetector.MutableReasonDetail;
import org.mutabilitydetector.findbugs.ThisPluginDetector.AnalysisSessionHolder;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.bcel.AnnotationDetector;

public class MutabilityDetectorFindBugsPlugin extends AnnotationDetector {

    private static final int PRIORITY_TO_REPORT = Priorities.NORMAL_PRIORITY;
    private final BugReporter bugReporter;
    private final Detector pluginToRegisterBugsWith;
    private final AnalysisSessionHolder analysisSessionHolder;
    
    private boolean doMutabilityDetectionOnCurrentClass;

    public MutabilityDetectorFindBugsPlugin(Detector pluginToRegisterBugsWith, BugReporter bugReporter, AnalysisSessionHolder analysisSessionHolder) {
        this.pluginToRegisterBugsWith = pluginToRegisterBugsWith;
        this.bugReporter = bugReporter;
        this.analysisSessionHolder = analysisSessionHolder;
    }

    
    @Override
    public void visitAnnotation(String annotationClass, Map<String, Object> map, boolean runtimeVisible) {
        super.visitAnnotation(annotationClass, map, runtimeVisible);
        
        doMutabilityDetectionOnCurrentClass = annotationClass.equals("Immutable") || annotationClass.endsWith(".Immutable");
    }
   
    public void visitClassContext(ClassContext classContext) {
        doMutabilityDetectionOnCurrentClass = false;

        super.visitClassContext(classContext);

        if (doMutabilityDetectionOnCurrentClass) {
            doMutabilityAnalysis(classContext);
        }
    }
    
    private void doMutabilityAnalysis(ClassContext classContext) {
        String toAnalyse = classContext.getClassDescriptor().getDottedClassName();
        AnalysisResult result = analysisSessionHolder.lazyGet().resultFor(dotted(toAnalyse));
        
        if (result.isImmutable != IsImmutable.IMMUTABLE) {
            
            for (MutableReasonDetail reasonDetail : result.reasons) {
                BugInstance bugInstance = new BugInstance(pluginToRegisterBugsWith, 
                                                          "MUTDEC_"+ reasonDetail.reason().code(), 
                                                          PRIORITY_TO_REPORT)
                    .addClass(classContext.getClassDescriptor());
                
                bugReporter.reportBug(bugInstance);
            }
        }
    }
   
    
    public void report() { }
    
}