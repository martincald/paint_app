/**
 * HapticFeedback.m
 *
 * JNI bridge to macOS NSHapticFeedbackManager.
 * Compile with:
 *   clang -dynamiclib -o libHapticFeedback.dylib HapticFeedback.m \
 *         -framework AppKit \
 *         -I"$JAVA_HOME/include" \
 *         -I"$JAVA_HOME/include/darwin"
 *
 * The resulting libHapticFeedback.dylib should be placed either:
 *   (a) next to the Paint App.jar and launched with -Djava.library.path=. , OR
 *   (b) inside the jar at /native/libHapticFeedback.dylib  (HapticFeedback.java
 *       will extract it to a temp file automatically).
 */

#import <AppKit/AppKit.h>
#include <jni.h>

/*
 * Class:     com_martinpaint_app_HapticFeedback
 * Method:    performHaptic
 * Signature: (I)V
 *
 * pattern values (mirror Java constants):
 *   0 = NSHapticFeedbackPatternGeneric
 *   1 = NSHapticFeedbackPatternAlignment
 *   2 = NSHapticFeedbackPatternLevelChange
 */
JNIEXPORT void JNICALL
Java_com_martinpaint_app_HapticFeedback_performHaptic(JNIEnv *env, jclass cls, jint pattern)
{
    @autoreleasepool {
        NSHapticFeedbackPattern hapticPattern;

        switch (pattern) {
            case 1:  hapticPattern = NSHapticFeedbackPatternAlignment;   break;
            case 2:  hapticPattern = NSHapticFeedbackPatternLevelChange; break;
            default: hapticPattern = NSHapticFeedbackPatternGeneric;     break;
        }

        [[NSHapticFeedbackManager defaultPerformer]
            performFeedbackPattern:hapticPattern
                   performanceTime:NSHapticFeedbackPerformanceTimeDefault];
    }
}
