# Preserve source locations for retracing obfuscated release stack traces.
# Archive the release mapping.txt alongside each published build.
-keepattributes SourceFile,LineNumberTable

# Dependencies provide their own consumer rules. Firestore data is mapped
# explicitly, so application models do not need reflection-based keep rules.
# Add narrowly scoped rules here if reflection is introduced in application code.
