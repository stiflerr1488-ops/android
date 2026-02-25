package com.example.teamcompass.ui

import com.example.teamcompass.core.TargetFilterState
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.IntrinsicsKt
import kotlin.jvm.internal.SourceDebugExtension
import kotlinx.coroutines.flow.FlowCollector

// $VF: Class flags could not be determined
@SourceDebugExtension(["SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 TeamCompassViewModel.kt\ncom/example/teamcompass/ui/TeamCompassViewModel\n*L\n1#1,49:1\n50#2:50\n1578#3:51\n*E\n"])
internal class `TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2`<T> : FlowCollector {
   fun `TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2`(`$receiver`: FlowCollector) {
      this.$this_unsafeFlow = `$receiver`;
   }

   fun emit(value: Any, `$completion`: Continuation): Any {
      var `$continuation`: Continuation;
      label20: {
         if (`$completion` is TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2$1) {
            `$continuation` = `$completion` as TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2$1;
            if (((`$completion` as TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2$1).label and Integer.MIN_VALUE) != 0) {
               `$continuation`.label -= Integer.MIN_VALUE;
               break label20;
            }
         }

         `$continuation` = new TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2$1(this, `$completion`);
      }

      val `$result`: Any = `$continuation`.result;
      val var5: Any = IntrinsicsKt.getCOROUTINE_SUSPENDED();
      switch ($continuation.label) {
         case 0:
            ResultKt.throwOnFailure(`$result`);
            val `$this$map_u24lambda_u245`: FlowCollector = this.$this_unsafeFlow;
            val var10001: TargetFilterState = (value as UiState).getTargetFilterState();
            `$continuation`.label = 1;
            if (`$this$map_u24lambda_u245`.emit(var10001, `$continuation`) === var5) {
               return var5;
            }
            break;
         case 1:
            ResultKt.throwOnFailure(`$result`);
            break;
         default:
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
      }

      return Unit.INSTANCE;
   }
}
