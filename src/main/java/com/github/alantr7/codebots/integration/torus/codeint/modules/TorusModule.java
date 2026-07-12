package com.github.alantr7.codebots.integration.torus.codeint.modules;

import com.github.alantr7.codebots.cbslang.exceptions.ExecutionException;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.Data;
import com.github.alantr7.codebots.cbslang.low.runtime.memory.DataType;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Context;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ExternalFunction;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.Module;
import com.github.alantr7.codebots.integration.torus.machine.ComputerInstance;

import java.util.UUID;

public class TorusModule extends Module {

    public TorusModule() {
        super("torus");
    }

    @Override
    public void setup() {
        registerFunction("request", new ExternalFunction(this, "request", DataType.INT, DataType.STRING, DataType.INT) {
            @Override
            public void prepareContext(Context context) {
                context.getMemory()[0] = new Data(DataType.INT, 0);
            }

            @Override
            public Data handle(Context context) throws ExecutionException {
                var computer = (ComputerInstance) context.getProgram().getExtra("computer");
                if (context.getMemory()[0].getValueAs(DataType.INT) == 0) {
                    UUID requestId = computer.createRequest(
                        context.getArgumentAs(0, DataType.STRING),
                        context.getArgumentAs(1, DataType.INT)
                    );
                    context.setRecall(true);
                    context.getMemory()[0].setValue(DataType.INT, 1);
                    context.getMemory()[1] = Data.of(requestId.toString());
                    return null;
                }

                ComputerInstance.DataRequest req = computer.getRequest(UUID.fromString(context.getMemory()[1].getValueAs(DataType.STRING)));
                if (!req.isCompleted()) {
                    context.setRecall(true);
                    return null;
                }

                return Data.of(req.getResponse());
            }
        });
    }

}
