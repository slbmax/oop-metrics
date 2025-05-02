package com.slbmax.metrics.calculator; 

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;

import java.util.*;
import java.util.stream.Collectors;

public class MetricsCalculator {

    public static void main(String[] args) {
        String[] packagesToScan = args.length > 0 ? args : new String[]{"com.slbmax.metrics.test"};

        System.out.println("Scanning packages: " + Arrays.toString(packagesToScan));

        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo() 
                .acceptPackages(packagesToScan)
                .scan()) 
        { 
            List<ClassInfo> classes = scanResult.getAllClasses().filter(ci -> !ci.isInterface() && !ci.isAnnotation() && !ci.isEnum());

            if (classes.isEmpty()) {
                System.out.println("No classes found in the specified packages.");
                return;
            }

            calculateAndPrintMetrics(classes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int calculateDIT(ClassInfo classInfo) {
        ClassInfo superClass = classInfo.getSuperclass();
        int dit;
        
        if (superClass == null || superClass.getName().equals("java.lang.Object")) {
            dit = 0; 
        } else {
             dit = calculateDIT(superClass) + 1;
        }
       
        return dit;
    }

    private static Map<String, Integer> calculateNOC(List<ClassInfo> classes) {
        Map<String, Integer> nocMap = new HashMap<>();
       
        classes.forEach(ci -> nocMap.put(ci.getName(), 0));

        
        for (ClassInfo classInfo : classes) {
            ClassInfo superClass = classInfo.getSuperclass();
            if (superClass != null) {
                String superClassName = superClass.getName();
                
                if (nocMap.containsKey(superClassName)) { 
                    nocMap.put(superClassName, nocMap.get(superClassName) + 1);
                }
            }
        }

        
        return nocMap;
    }

    private static List<MethodInfo> getAllMethods(ClassInfo classInfo) {
        // contain possible parent private methods
        List<MethodInfo> allPrivate = classInfo.getMethodInfo().filter(m -> !m.isConstructor() && !m.isStatic() && m.isPrivate());

        // contain only own private methods
        List<MethodInfo> declaredPrivate = classInfo.getDeclaredMethodInfo().filter(m -> !m.isConstructor() && !m.isStatic() && m.isPrivate());

        // contain only parent private

        List<MethodInfo> parentPrivate = allPrivate.stream()
                .filter(m -> !declaredPrivate.contains(m))
                .collect(Collectors.toList());

        // cleaned from invalid parent private methods
        return classInfo.getMethodInfo().filter(m -> !m.isConstructor() && !m.isStatic()).
               filter(m -> !parentPrivate.contains(m));
    }

    private static List<FieldInfo> getAllAttributes(ClassInfo classInfo) {
        // contain possible parent private attributes
        List<FieldInfo> allPrivate = classInfo.getFieldInfo().filter(f -> f.isPrivate());

        // contain only own private attributes
        List<FieldInfo> declaredPrivate = classInfo.getDeclaredFieldInfo().filter(f -> f.isPrivate());

        // contain only parent private attributes
        List<FieldInfo> parentPrivate = allPrivate.stream()
                .filter(f -> !declaredPrivate.contains(f))
                .collect(Collectors.toList());

        // cleaned from invalid parent private attributes

        return classInfo.getFieldInfo().filter(f -> !parentPrivate.contains(f));
    }
    
    private static List<MethodInfo> getInheritedMethods(ClassInfo classInfo) {
        List<MethodInfo> all = getAllMethods(classInfo);
        List<MethodInfo> declared =  classInfo.getDeclaredMethodInfo();
    

        List<MethodInfo> inherited = new ArrayList<>(all);
        for (MethodInfo method : declared) {
            if (all.contains(method)) {
                inherited.remove(method);
            }
        }

        return inherited;
    }

    private static List<MethodInfo> getOverridenMethods(ClassInfo classInfo) {
        ClassInfo superClass = classInfo.getSuperclass();
        if (superClass == null) {
            return Collections.emptyList();
        }

        List<MethodInfo> allSuper = getAllMethods(superClass);
        List<String> superMethodNames = allSuper.stream().map(MethodInfo::getName).collect(Collectors.toList());
        
        List<MethodInfo> declared = classInfo.getDeclaredMethodInfo();

        List<MethodInfo> overriden = new ArrayList<>();
        for (MethodInfo method : declared) {
            if (superMethodNames.contains(method.getName())) {
                overriden.add(method);
            }
        }

        return overriden;
    }

    private static List<MethodInfo> getNewMethods(ClassInfo classInfo) {
        if (classInfo.getSuperclass() == null) {
            return getAllMethods(classInfo);
        }

        List<MethodInfo> declared = classInfo.getDeclaredMethodInfo();
        List<MethodInfo> overriden = getOverridenMethods(classInfo);

        List<MethodInfo> newMethods = new ArrayList<>();
        for (MethodInfo method : declared) {
           if (!overriden.contains(method)) {
                newMethods.add(method);
            }
        }

        return newMethods;
    }

    private static List<FieldInfo> getInheritedAttributes(ClassInfo classInfo) {
        List<FieldInfo> all = getAllAttributes(classInfo);
        List<FieldInfo> declared = classInfo.getDeclaredFieldInfo();

        List<FieldInfo> inherited = new ArrayList<>(all);
        for (FieldInfo field : declared) {
            if (all.contains(field)) {
                inherited.remove(field);
            }
        }

        return inherited;
    }

    private static List<MethodInfo> getHiddenMethods(ClassInfo classInfo) {
        List<MethodInfo> allMethods = getAllMethods(classInfo);
        List<MethodInfo> hiddenMethods = new ArrayList<>();

        for (MethodInfo method : allMethods) {
            if (method.isPrivate() || method.isProtected()) {
                hiddenMethods.add(method);
            }
        }

        return hiddenMethods;
    }

    private static List<FieldInfo> getHiddenAttributes(ClassInfo classInfo) {
        List<FieldInfo> allAttributes = getAllAttributes(classInfo);
        List<FieldInfo> hiddenAttributes = new ArrayList<>();

        for (FieldInfo field : allAttributes) {
            if (field.isPrivate() || field.isProtected()) {
                hiddenAttributes.add(field);
            }
        }

        return hiddenAttributes;
    }

    private static void calculateAndPrintMetrics(List<ClassInfo> classes) {
        long totalMethods = 0;
        long totalHiddenMethods = 0;
        long totalInheritedMethods = 0;
        long totalAttributes = 0;
        long totalHiddenAttributes = 0;
        long totalInheritedAttributes = 0;
        long totalNewMethods = 0;
        long totalOverriddenMethods = 0;
        long totalChildren = 0;

        System.out.println("-----Class metrics:-----");

        Map<String, Integer> nocMap = calculateNOC(classes);

        for (ClassInfo classInfo : classes) {
            System.out.println("Class: " + classInfo.getName());
            System.out.println("  DIT: " + calculateDIT(classInfo));
            System.out.println("  NOC: " + nocMap.get(classInfo.getName()));

            System.out.println("  Number of Methods: " + getAllMethods(classInfo).size());
            System.out.println("  Hidden Methods: " + getHiddenMethods(classInfo).size());
            System.out.println("  Inherited Methods: " + getInheritedMethods(classInfo).size());
            System.out.println("  Number of Attributes: " + getAllAttributes(classInfo).size());
            System.out.println("  Hidden Attributes: " + getHiddenAttributes(classInfo).size());
            System.out.println("  Inherited Attributes: " + getInheritedAttributes(classInfo).size());
            System.out.println("  New Methods: " + getNewMethods(classInfo).size());
            System.out.println("  Overridden Methods: " + getOverridenMethods(classInfo).size());

            totalMethods += getAllMethods(classInfo).size();
            totalHiddenMethods += getHiddenMethods(classInfo).size();
            totalInheritedMethods += getInheritedMethods(classInfo).size();
            totalAttributes += getAllAttributes(classInfo).size();
            totalHiddenAttributes += getHiddenAttributes(classInfo).size();
            totalInheritedAttributes += getInheritedAttributes(classInfo).size();
            totalNewMethods += getNewMethods(classInfo).size();
            totalOverriddenMethods += getOverridenMethods(classInfo).size();
            totalChildren += nocMap.get(classInfo.getName());
        }

        System.out.println("-----MOOD metrics:-----");
        double mhf = (totalMethods == 0) ? 0 : (double) totalHiddenMethods / totalMethods;
        double mif = (totalMethods == 0) ? 0 : (double) totalInheritedMethods / totalMethods;
        double ahf = (totalAttributes == 0) ? 0 : (double) totalHiddenAttributes / totalAttributes;
        double aif = (totalAttributes == 0) ? 0 : (double) totalInheritedAttributes / totalAttributes;
        double pof = (totalNewMethods * totalChildren == 0) ? 0 : (double) totalOverriddenMethods / (totalNewMethods * totalChildren);

        System.out.printf("  MHF: %.4f\n", mhf);
        System.out.printf("  MIF: %.4f\n", mif);
        System.out.printf("  AHF: %.4f\n", ahf);
        System.out.printf("  AIF: %.4f\n", aif);
        System.out.printf("  POF: %.4f\n", pof);
    }
}