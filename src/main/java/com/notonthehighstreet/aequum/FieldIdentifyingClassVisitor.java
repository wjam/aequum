package com.notonthehighstreet.aequum;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;

class FieldIdentifyingClassVisitor extends ClassVisitor {
    private final String name;
    private final String signature;

    private Optional<String> fieldName = Optional.empty();

    public FieldIdentifyingClassVisitor(final String name, final String signature) {
        super(Opcodes.ASM5);
        this.name = name;
        this.signature = signature;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        if (Objects.equals(name, this.name) && Objects.equals(desc, this.signature) && ((access & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC)) {
            return new MethodVisitor(Opcodes.ASM5) {
                @Override
                public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
                    super.visitFieldInsn(opcode, owner, name, desc);
                    fieldName = Optional.of(name);
                }

                @Override
                public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);

                    // The lambda may just be a call to a bridge method which contains the field load we're after, so follow it down.
                    final FieldIdentifyingClassVisitor subVisitor = new FieldIdentifyingClassVisitor(name, desc);
                    try {
                        new ClassReader(owner).accept(subVisitor, 0);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }

                    if (subVisitor.fieldName.isPresent()) {
                        fieldName = subVisitor.fieldName;
                    }

                }

            };
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public Optional<String> getFieldName() {
        return fieldName;
    }
}
