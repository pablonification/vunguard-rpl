'use server'

import { revalidatePath } from 'next/cache'
import { deleteAccount } from '@/lib/db/models/account' // We will create this function next
import { requireAuth } from '@/lib/auth'

export async function deleteAccountAction(accountId: number) {
  const session = await requireAuth(['admin']) // Ensure only admin can delete

  if (!session) {
    throw new Error('Unauthorized')
  }

  // Prevent admin from deleting their own account?
  // if (session.id === accountId) {
  //   throw new Error('Cannot delete your own account.')
  // }
  
  try {
    await deleteAccount(accountId)
    revalidatePath('/dashboard/accounts') // Revalidate the accounts page cache
    return { success: true }
  } catch (error) {
    console.error('Failed to delete account:', error)
    // Provide a more specific error message if possible
    if (error instanceof Error) {
        throw new Error(`Failed to delete account: ${error.message}`)
    } else {
        throw new Error('An unknown error occurred while deleting the account.')
    }
  }
} 