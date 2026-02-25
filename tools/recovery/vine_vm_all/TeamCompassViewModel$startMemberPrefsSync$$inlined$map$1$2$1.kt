package com.example.teamcompass.ui

import kotlin.coroutines.Continuation
import kotlin.coroutines.jvm.internal.ContinuationImpl
import kotlin.coroutines.jvm.internal.DebugMetadata

// $VF: Class flags could not be determined
@DebugMetadata(f = "TeamCompassViewModel.kt", l = [50], i = [], s = [], n = [], m = "emit", c = "com.example.teamcompass.ui.TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2")
internal class `TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2$1` : ContinuationImpl {
   open int label;
   open Object L$0;

   fun `TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2$1`(
      `this$0`: TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2, `$completion`: Continuation
   ) {
      super(`$completion`);
      this.this$0 = `this$0`;
   }

   fun invokeSuspend(`$result`: Any): Any {
      this.result = `$result`;
      this.label |= Integer.MIN_VALUE;
      return this.this$0.emit(null, this as Continuation);
   }
}
