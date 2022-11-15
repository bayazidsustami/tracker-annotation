package com.example.trackerprocessor

import com.example.trackerannotation.Tracker
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName

class TrackerAnnotationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
): SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val symbols = resolver.getSymbolsWithAnnotation(Tracker::class.qualifiedName!!)
        val unableToProcess = symbols.filterNot { it.validate() }

        val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())

        symbols.filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(TrackerAnnotationVisitor(dependencies), Unit) }

        return unableToProcess.toList()
    }

    private inner class TrackerAnnotationVisitor(val dependencies: Dependencies): KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val packageName = classDeclaration.packageName.asString()
            val className = classDeclaration.toClassName()
            val classNameGenerated = className.simpleName
                .removePrefix("[")
                .removeSuffix("]")

            val childClass = classDeclaration.getSealedSubclasses()

            val objBuilder = TypeSpec.objectBuilder("${classNameGenerated}Analytic")

            childClass.toList().forEach {
                objBuilder.addFunction(generateFunction(it))
            }

            val builderClass = objBuilder.build()

            val file = FileSpec.builder(packageName, className.simpleName)
                .addType(builderClass)
                .build()

            val outputStream = codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName,
                fileName = "${className.simpleName}Analytic"
            )
            outputStream.use { os -> os.writer().use { file.writeTo(it) } }
        }

        private fun generateFunction(childClass: KSClassDeclaration): FunSpec {
            val name = childClass.simpleName.asString().capitalize()
            val parameter = ParameterSpec.builder("model", childClass.toClassName())
                .build()

            val funSpec = FunSpec.builder(name)
                .addStatement("val data = mapOf(")

            childClass.getAllProperties().forEach {
                val propName = it.simpleName.asString()

                if (it.isNotKotlinPrimitive()) {
                    funSpec.addStatement("\t \"$propName\" to mapOf<String, Any>()")
                } else {
                    funSpec.addStatement(generateMapping(propName, "${parameter.name}.$propName"))
                }
            }

            funSpec.addStatement(")")

            return funSpec.addParameter(parameter)
                .addStatement("TrackApp.send(data)")
                .build()
        }

        private fun generateMapping(key: String, value: String) : String {
            return "\t \"$key\" to $value,"
        }

        private fun String.capitalize(): String {
            return this.replaceFirstChar { it.lowercase() }
        }

        private fun KSPropertyDeclaration.isNotKotlinPrimitive(): Boolean {
            return when(type.element?.toString()) {
                "String",
                "Int",
                "Short",
                "Number",
                "Boolean",
                "Byte",
                "Char",
                "Float",
                "Double",
                "Long",
                "Unit",
                "Any" -> false
                else -> true
            }
        }
    }
}
