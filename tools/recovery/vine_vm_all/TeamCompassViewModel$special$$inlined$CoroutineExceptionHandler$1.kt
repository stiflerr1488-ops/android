package com.example.teamcompass.ui

import android.util.Log
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.internal.SourceDebugExtension
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineExceptionHandler.Key

@SourceDebugExtension(["SMAP\nCoroutineExceptionHandler.kt\nKotlin\n*S Kotlin\n*F\n+ 1 CoroutineExceptionHandler.kt\nkotlinx/coroutines/CoroutineExceptionHandlerKt$CoroutineExceptionHandler$1\n+ 2 TeamCompassViewModel.kt\ncom/example/teamcompass/ui/TeamCompassViewModel\n*L\n1#1,48:1\n78#2,2:49\n*E\n"])
public class `TeamCompassViewModel$special$$inlined$CoroutineExceptionHandler$1` : AbstractCoroutineContextElement, CoroutineExceptionHandler {
   fun `TeamCompassViewModel$special$$inlined$CoroutineExceptionHandler$1`(`$super_call_param$1`: Key) {
      super(`$super_call_param$1` as kotlin.coroutines.CoroutineContext.Key);
   }

   public open fun handleException(context: CoroutineContext, exception: Throwable) {
      Log.e("TeamCompassVM", "Unhandled coroutine exception", exception);
   }
}
