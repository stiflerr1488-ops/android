package com.example.teamcompass.ui;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;

@Metadata(
   mv = {2, 0, 0},
   k = 3,
   xi = 48
)
@DebugMetadata(
   f = "TeamCompassViewModel.kt",
   l = {50},
   i = {},
   s = {},
   n = {},
   m = "emit",
   c = "com.example.teamcompass.ui.TeamCompassViewModel$bindSavedStateHandle$1$invokeSuspend$$inlined$map$1$2"
)
public final class TeamCompassViewModel$bindSavedStateHandle$1$invokeSuspend$$inlined$map$1$2$1 extends ContinuationImpl {
   // $FF: synthetic field
   Object result;
   int label;
   Object L$0;
   // $FF: synthetic field
   final TeamCompassViewModel$bindSavedStateHandle$1$invokeSuspend$$inlined$map$1$2 this$0;

   public TeamCompassViewModel$bindSavedStateHandle$1$invokeSuspend$$inlined$map$1$2$1(TeamCompassViewModel$bindSavedStateHandle$1$invokeSuspend$$inlined$map$1$2 this$0, Continuation $completion) {
      super($completion);
      this.this$0 = this$0;
   }

   public final Object invokeSuspend(Object $result) {
      this.result = $result;
      this.label |= Integer.MIN_VALUE;
      return this.this$0.emit((Object)null, (Continuation)this);
   }
}
