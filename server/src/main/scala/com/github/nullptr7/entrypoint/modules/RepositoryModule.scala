package com.github.nullptr7
package entrypoint
package modules

import org.typelevel.log4cats.Logger

import cats.effect.kernel.{Async, Resource}

import skunk.Session

import models._
import storage.{AddressRepository, EmployeeRepository}

abstract class RepositoryModule[F[_]] private (
  employeeRepository: EmployeeRepository[F],
  addressRepository:  AddressRepository[F]
) {

  def findAllEmployees: F[List[Employee]] = employeeRepository.findAllEmployees

  def addEmployee(createEmployee: CreateEmployee): F[EmployeeId] = employeeRepository.addEmployee(createEmployee)

  def findEmployeeById(id: Long): F[Option[Employee]] = employeeRepository.findById(id)

  def addAddress(createAddress: CreateAddress): F[AddressId] = addressRepository.addAddress(createAddress)

  def findAddressById(id: AddressId): F[Option[Address]] = addressRepository.findAddressById(id)

  def findAddressByZip(pincode: String): F[Option[Address]] = addressRepository.findAddressByZip(pincode)
}

object RepositoryModule {

  def make[F[_]: Async: Logger](session: Session[F]): Resource[F, RepositoryModule[F]] =
    Resource.pure(
      new RepositoryModule[F](EmployeeRepository(session), AddressRepository(session)) {}
    )

}
