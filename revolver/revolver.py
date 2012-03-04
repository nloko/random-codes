#!/usr/bin/python

import shelve
import sys

STORE = 'store'
NEXT_KEY = 'next'
LIST_KEY = 'list'

def getStore():
    return shelve.open(STORE)

def getNext(store=getStore()):
    next = 0

    if store.has_key(NEXT_KEY):
        next = store[NEXT_KEY]

    return next

def getList(store=getStore()):

    if store.has_key(LIST_KEY):
        return store[LIST_KEY]

    return []

def doInput(input, store=getStore()):
    if sys.stdin.isatty():
        raise Exception, 'no input.'

    list = []
    for line in sys.stdin:
        if len(line) == 0: continue
        list.append(line.rstrip('\n'))

    if len(list) == 0:
        raise Exception, 'input is empty.'

    try:
        store[LIST_KEY] = list
        store[NEXT_KEY] = 0
    finally:
        store.close()

def doList(input, store=getStore()):
    list = getList(store)
    for i in range(len(list)):
        print '%i\t%s' % (i, list[i])

def doDone(input, store=getStore()):
    list = getList(store)
    next = getNext(store)

    print '%s is done.' % list[next]

    if next == len(list) - 1:
        next = 0
    else:
        next += 1

    store[NEXT_KEY] = next

def doUpdate(input, store=getStore()):
    if len(input) < 2:
        raise Exception, 'index must follow. Use \'list\' to show indicies.'

    index = input[1]
    if not index.isdigit():
        raise Exception, 'index not found.'

    list = getList(store)
    index = min(max(0, int(index)), len(list) - 1)
    store[NEXT_KEY] = index

    print 'next updated to %s' % list[index]
    
def doNext(input, store=getStore()):
    list = getList(store)
    next = getNext(store)

    print '%s is next.' % list[next] 

def main():
    cmd = 'next'
    rc = 0
    numberOfArgs = len(sys.argv) - 1

    if numberOfArgs > 0:
        cmd = sys.argv[1]

    input = sys.argv[1:]

    if cmd == 'input':
        try:
            doInput(input)
        except Exception, e:
            print e
            return 1

        return 0

    store = getStore()

    if len(getList(store)) == 0:
        print 'list is empty.'
        store.close()
        return 1

    try:
        if cmd == 'list': doList(input, store)
        elif cmd == 'done': doDone(input, store)
        elif cmd == 'update': doUpdate(input, store)
        else: doNext(input, store)
    except Exception, e:
        print e
        rc = 1
    finally:
        store.close()

    return rc

sys.exit(main())
