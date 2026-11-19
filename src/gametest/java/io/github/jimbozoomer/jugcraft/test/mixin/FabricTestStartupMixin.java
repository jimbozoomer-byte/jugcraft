package io.github.jimbozoomer.jugcraft.test.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Wait for Fabric's test thread to join the phaser before the renderer advances the first tick. */
@Mixin(value = ThreadingImpl.class, remap = false)
public abstract class FabricTestStartupMixin {
	@Unique private static volatile CountDownLatch jugcraftTest$registration;

	@WrapOperation(method = "runTestThread", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"))
	private static void jugcraftTest$startRegisteredThread(Thread thread, Operation<Void> start) {
		CountDownLatch registration = new CountDownLatch(1);
		jugcraftTest$registration = registration;
		try {
			start.call(thread);
			if (!registration.await(30, TimeUnit.SECONDS)) {
				throw new IllegalStateException("Fabric's client test thread did not register with its phaser");
			}
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while starting the client test thread", interrupted);
		}
	}

	@Inject(method = "lambda$runTestThread$0", at = @At(value = "INVOKE",
			target = "Ljava/util/concurrent/Phaser;register()I", shift = At.Shift.AFTER))
	private static void jugcraftTest$registered(Runnable test, CallbackInfo info) {
		jugcraftTest$registration.countDown();
	}
}
