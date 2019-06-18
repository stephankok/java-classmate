import org.junit.runner.Result;

import com.fasterxml.classmate.*;
import com.fasterxml.classmate.members.*;
import com.fasterxml.classmate.types.*;
import com.fasterxml.classmate.util.*;

public class TestRunner {
	private final static int AMOUNTOFTESTS = 226;
	
	public static void main(String[] args) 
	{
//		Check arguments
		int repeats = validateArgument(args);
		
		System.out.println("Repeating " + repeats + " times.");
		runTests(repeats);
		
		System.exit(0);
	}

	public static int validateArgument(String[] args) {
		if (args.length != 1) {
			System.out.println("You must specify the amount of loops by parsing one argument");
			System.exit(1);
		}
		
		try {
			return Integer.parseInt(args[0]);			
		}
		catch (NumberFormatException e) {
			System.out.println("Parsed argument is not an integer: " + e.getMessage());
			System.exit(2);
		}
		
		return 0;
	}

	public static void runTests(int repeats) {
		for (int i = 0; i < repeats; i++) {
			Result result = runtests();
			if (!result.wasSuccessful() || result.getRunCount() != AMOUNTOFTESTS) {
				printResult(result, i);
				System.exit(3);
			}
		}
	}
	
	private static void printResult(Result result, int run) {
		System.out.println("Failed to run tests all " + AMOUNTOFTESTS + " tests succesfull");
		System.out.println("Run: " + run);
		System.out.println("Fails: " + result.getFailureCount());
		System.out.println("Runtime: " + result.getRunTime());
		System.out.println("RunCount: " + result.getRunCount());
		System.out.println("IgnoreCount: " + result.getIgnoreCount() + "\n");
		System.out.print("Failed runs: ");
		for (int i = 0; i < result.getFailures().size(); i++) {			
			System.out.println("    " + result.getFailures().get(i));			
			System.out.println("    " + result.getFailures().get(i).getMessage());
			System.out.println("    " + result.getFailures().get(i).getTrace());
			System.out.println("    " + result.getFailures().get(i).getTestHeader());
			System.out.println("    ");
		}
	}
	
	private static Result runtests() {
//		com.fasterxml.classmate
		return org.junit.runner.JUnitCore.runClasses(
				AnnotationInclusionTest.class,
				AnnotationsTest.class,
//				BaseTest.class,
				OddJDKTypesTest.class,
				ResolvedTypeTest.class,
				ResolvedTypeWithMembersTest.class,
				StdConfigurationTest.class,
				TestMemberGenericTypes.class,
				TestMemberResolver.class,
				TestParameterAnnotations.class,
				TestReadme.class,
				TestSelfRefMemberTypes.class,
				TestSubtypeResolution.class,
				TestTypeDescriptions.class,
				TestTypeResolver.class,
				TypeBindingsTest.class,
//		com.fasterxml.class,mate.members
				GhostTypeParameterInFieldTest.class,
				HierarchicTypeTest.class,
				Issue28Test.class,
				RawConstructorTest.class,
				RawFieldTest.class,
				RawMemberTest.class,
				RawMethodTest.class,
				ResolvedConstructorTest.class,
				ResolvedFieldTest.class,
				ResolvedMemberTest.class,
				ResolvedMethodTest.class,
//		com.fasterxml.class,mate.types
				ResolvedArrayTypeTest.class,
				ResolvedInterfaceTypeTest.class,
				ResolvedObjectTypeTest.class,
				ResolvedPrimitiveTypeTest.class,
				ResolvedRecursiveTypeTest.class,
				TypePlaceHolderTest.class,
//		com.fasterxml.classmate.util
				ClassKeyTest.class,
				MethodKeyTest.class,
				TestResolvedTypeCache.class);
	}
}