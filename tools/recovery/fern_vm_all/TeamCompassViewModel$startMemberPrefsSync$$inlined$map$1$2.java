package com.example.teamcompass.ui;

import com.example.teamcompass.core.TargetFilterState;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlinx.coroutines.flow.FlowCollector;

@Metadata(
   mv = {2, 0, 0},
   k = 3,
   xi = 48
)
@SourceDebugExtension({"SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 TeamCompassViewModel.kt\ncom/example/teamcompass/ui/TeamCompassViewModel\n*L\n1#1,49:1\n50#2:50\n1578#3:51\n*E\n"})
public final class TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2 implements FlowCollector {
   // $FF: synthetic field
   final FlowCollector $this_unsafeFlow;

   public TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2(FlowCollector $receiver) {
      this.$this_unsafeFlow = $receiver;
   }

   public final Object emit(Object value, Continuation $completion) {
      label20: {
         if ($completion instanceof Continuation $continuation) {
            if (($continuation.label & Integer.MIN_VALUE) != 0) {
               $continuation.label -= Integer.MIN_VALUE;
               break label20;
            }
         }

         $continuation = new TeamCompassViewModel$startMemberPrefsSync$$inlined$map$1$2$1(this, $completion);
      }

      Object $result = $continuation.result;
      Object var5 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
      switch ($continuation.label) {
         case 0:
            ResultKt.throwOnFailure($result);
            FlowCollector $this$map_u24lambda_u245 = this.$this_unsafeFlow;
            int var12 = 0;
            Continuation var10001 = $continuation;
            UiState it = (UiState)value;
            int var11 = 0;
            TargetFilterState var13 = it.getTargetFilterState();
            $continuation.label = 1;
            if ($this$map_u24lambda_u245.emit(var13, $continuation) == var5) {
               return var5;
            }
            break;
         case 1:
            int var8 = 0;
            ResultKt.throwOnFailure($result);
            break;
         default:
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
      }

      return Unit.INSTANCE;
   }
}
