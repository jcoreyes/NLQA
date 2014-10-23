__author__ = 'coreyesj'

def insertInterval(lst, a):
    newlst = []
    for i in lst:
        if a[0] >= i[1] or a[1] <= a[1]:
            newlst.append(i)
        else:
            mergeStart = min(i[0], a[0])
            mergeEnd = max(i[1], i[0])

    newlst.append((mergeStart, mergeEnd))
    return newlst


def test():
    lst = [(1, 3), (4, 6), (7, 10)]
    print insertInterval(lst, (2, 5))

test()