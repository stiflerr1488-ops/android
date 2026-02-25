/*
$VF: Unable to decompile class
Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
java.lang.NullPointerException: Cannot invoke "String.equals(Object)" because "n.simpleName" is null
  at org.vineflower.kotlin.KotlinWriter.lambda$writeClass$0(KotlinWriter.java:265)
  at java.base/java.util.stream.ReferencePipeline$2$1.accept(Unknown Source)
  at java.base/java.util.ArrayList$ArrayListSpliterator.tryAdvance(Unknown Source)
  at java.base/java.util.stream.ReferencePipeline.forEachWithCancel(Unknown Source)
  at java.base/java.util.stream.AbstractPipeline.copyIntoWithCancel(Unknown Source)
  at java.base/java.util.stream.AbstractPipeline.copyInto(Unknown Source)
  at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(Unknown Source)
  at java.base/java.util.stream.FindOps$FindOp.evaluateSequential(Unknown Source)
  at java.base/java.util.stream.AbstractPipeline.evaluate(Unknown Source)
  at java.base/java.util.stream.ReferencePipeline.findAny(Unknown Source)
  at org.vineflower.kotlin.KotlinWriter.writeClass(KotlinWriter.java:266)
  at org.jetbrains.java.decompiler.main.ClassesProcessor.writeClass(ClassesProcessor.java:500)
  at org.jetbrains.java.decompiler.main.Fernflower.getClassContent(Fernflower.java:196)
  at org.jetbrains.java.decompiler.struct.ContextUnit.lambda$save$3(ContextUnit.java:195)
*/