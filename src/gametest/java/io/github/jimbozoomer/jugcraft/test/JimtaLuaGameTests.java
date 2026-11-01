package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.jimtafacts.computers.lua.LuaProcess;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/** Real isolated subprocess checks. Headless game ticks are unpaced, unlike child JVM execution. */
public final class JimtaLuaGameTests {
    @GameTest
    public void isolatedLuaResumesCoroutinesAndBoundsAllocationFailure(GameTestHelper h) throws InterruptedException {
        var messages = new ArrayList<String>();
        try (var worker = LuaProcess.open("assert(vector.new(1,2,3):dot(vector.new(4,5,6))==32); assert(colors.combine(colors.red,colors.blue)==18432); assert(textutils.unserializeJSON('{\"value\":7}').value==7); assert(fs.isReadOnly('rom/programs/turtle/go.lua')); local sum=0; for i=1,5000 do sum=sum+i end; local child=coroutine.create(function() print('child'); print('continued') end); assert(coroutine.resume(child)); print(sum); assert(io==nil and package.loadlib==nil and debug.sethook==nil)")) {
            long deadline = deadline();
            while (true) {
                var reply = await(worker, deadline);
                h.assertTrue(reply.status() != 3, "Lua worker returned an error: " + reply.text());
                if (reply.status() == 1 && reply.text().startsWith("print:")) messages.add(reply.text());
                if (reply.status() == 2) break;
                worker.resume(true, hostReply(reply.text()));
            }
        }
        h.assertTrue(messages.size() == 3 && messages.get(0).equals("print:child")
                && messages.get(1).equals("print:continued") && messages.get(2).equals("print:12502500"),
                "Host calls inside a child coroutine must resume exactly once, with arithmetic preserved");
        boolean ready = false;
        try (var worker = LuaProcess.open("print('allocation-ready'); local value=string.rep('x',134217728); print(#value)")) {
            long deadline = deadline();
            while (true) {
                LuaProcess.Reply reply;
                try {
                    reply = await(worker, deadline);
                } catch (IllegalArgumentException expectedWorkerFailure) {
                    h.assertTrue(ready, "Worker startup/API failure is not evidence of an allocation bound");
                    break;
                }
                if (reply.status() == 3) {
                    h.assertTrue(ready, "Runtime must reach allocation sentinel before its bounded failure");
                    break;
                }
                if (reply.status() == 1) {
                    h.assertTrue(reply.text().equals("kind:") || reply.text().equals("existsfile:.settings")
                            || reply.text().startsWith("terminal:") || reply.text().equals("print:allocation-ready"),
                            "Oversized allocation cannot emit a result");
                    if (reply.text().equals("print:allocation-ready")) ready = true;
                }
                h.assertTrue(reply.status() != 2, "Oversized allocation cannot complete");
                worker.resume(true, hostReply(reply.text()));
            }
        }
        h.succeed();
    }

    @GameTest
    public void originalRomShellListsFilesAndAcceptsExit(GameTestHelper h) throws InterruptedException {
        boolean finished = false;
        try (var worker = LuaProcess.open("assert(shell.run('shell','list','rom')); assert(shell.run('shell')); print('shell-finished')")) {
            long deadline = deadline();
            while (true) {
                var reply = await(worker, deadline);
                h.assertTrue(reply.status() != 3, "ROM shell failed: " + reply.text());
                if (reply.text().equals("print:shell-finished")) finished = true;
                if (reply.status() == 2) break;
                worker.resume(true, reply.text().equals("event:") ? "jugcraft_line\t0\t0\texit" : hostReply(reply.text()));
            }
        }
        h.assertTrue(finished, "Both original shell paths completed");
        h.succeed();
    }

    private static long deadline() { return System.nanoTime() + TimeUnit.SECONDS.toNanos(30); }

    // These tests touch no world state while waiting. A bounded sleep yields CPU to
    // the isolated JVM instead of exhausting an unpaced GameTest tick budget.
    // LuaProcess's own startup and per-response watchdogs remain active throughout.
    private static LuaProcess.Reply await(LuaProcess worker, long deadline) throws InterruptedException {
        while (System.nanoTime() - deadline < 0) {
            var reply = worker.poll();
            if (reply != null) return reply;
            Thread.sleep(5);
        }
        throw new AssertionError("Lua program exceeded its 30-second wall-clock test budget");
    }

    private static String hostReply(String request) {
        if (request.equals("terminal:getTextColor")) return "1";
        if (request.equals("terminal:getBackgroundColor")) return "32768";
        if (request.equals("kind:")) return "turtle_normal";
        if (request.equals("terminal:getSize")) return "48\t24";
        if (request.equals("terminal:getCursorPos")) return "1\t1";
        if (request.equals("terminal:isColor")) return "true";
        return "";
    }
}
