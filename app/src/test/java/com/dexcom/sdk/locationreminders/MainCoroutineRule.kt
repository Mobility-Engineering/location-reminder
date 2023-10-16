package com.dexcom.sdk.locationreminders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineExceptionHandler
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.createTestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/*@ExperimentalCoroutinesApi
class ExampleTest {
    val testScheduler = TestCoroutineScheduler()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)

    @Test
    fun someTest() = testScope.runTest {
        // ...
    }
}

 */

class MainCoroutineRule(val dispatcher: TestDispatcher = StandardTestDispatcher())://UnconfinedTestDispatcher())://StandardTestDispatcher()):
    TestWatcher(){//},//{//,
    /*This should not break anything, as TestCoroutineScope is now defined in terms of createTestCoroutineScope.
If it does break something, it means that you already supplied a TestCoroutineScheduler to some scope; in this
case, also pass this scheduler as the argument to the dispatcher.

     */
    //TestCoroutineScope by createTestCoroutineScope(dispatcher) {
//TestScope() {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    /*
       Remove all calls to TestCoroutineScope.cleanupTestCoroutines from the code base. Instead,
       as the last step of each test, do return scope.runTest;
       if possible, the whole test body should go inside the runTest block.

        */
    /* As a side note:
    Replace runBlockingTest with runBlockingTestOnTestScope, createTestCoroutineScope with TestScope
    Also, replace runTestWithLegacyScope with just runTest. All of this can be done in parallel
    with replacing runBlockingTest with runTest.

    This step should remove all uses of TestCoroutineScope, explicit or implicit.
     */
    override fun finished(description: Description?) {
        super.finished(description)
        //cleanupTestCoroutines()

        Dispatchers.resetMain()
    }
    /*
    BEFORE:
    val dispatcher = StandardTestDispatcher()
    val scope = TestScope(dispatcher)


    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testFoo() = scope.runTest {
        // ...
    }

     */


/*
    AFTER:
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testFoo() = runTest {
        // ...
    }

 */
}

/*
@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()):
    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(dispatcher) {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}

 */

